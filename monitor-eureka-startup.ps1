# Eureka Server Startup Monitor and Diagnostics

Write-Host "🔍 Eureka Server Startup Monitor" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# Function to check Docker container status
function Check-ContainerStatus {
    param($serviceName)
    
    $status = docker ps --filter "name=$serviceName" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    if ($status) {
        Write-Host "Container Status:" -ForegroundColor Green
        Write-Host $status -ForegroundColor White
        return $true
    } else {
        Write-Host "❌ Container not running" -ForegroundColor Red
        return $false
    }
}

# Function to check health endpoint
function Check-Health {
    param($url, $serviceName)
    
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -ErrorAction Stop
        Write-Host "✅ $serviceName health check passed (Status: $($response.StatusCode))" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "❌ $serviceName health check failed: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to show logs
function Show-RecentLogs {
    param($serviceName, $lines = 20)
    
    Write-Host ""
    Write-Host "📝 Recent logs for $serviceName (last $lines lines):" -ForegroundColor Yellow
    Write-Host "================================================" -ForegroundColor Yellow
    docker-compose logs --tail $lines $serviceName
    Write-Host "================================================" -ForegroundColor Yellow
}

# Start monitoring
$startTime = Get-Date
Write-Host "🚀 Starting Eureka Server monitoring at $($startTime.ToString('HH:mm:ss'))" -ForegroundColor Green
Write-Host ""

# Start the service
Write-Host "Starting Eureka Server..." -ForegroundColor Yellow
docker-compose up -d eureka-server

Start-Sleep 5

# Monitor startup progress
$maxWaitMinutes = 3
$checkInterval = 10
$maxChecks = ($maxWaitMinutes * 60) / $checkInterval
$currentCheck = 0

while ($currentCheck -lt $maxChecks) {
    $currentCheck++
    $elapsedTime = (Get-Date) - $startTime
    
    Write-Host ""
    Write-Host "🕐 Check $currentCheck/$maxChecks - Elapsed: $($elapsedTime.ToString('mm\:ss'))" -ForegroundColor Cyan
    
    # Check container status
    $containerRunning = Check-ContainerStatus "eureka-server"
    
    if ($containerRunning) {
        # Check health endpoint
        $healthOk = Check-Health "http://localhost:8761/actuator/health" "Eureka Server"
        
        if ($healthOk) {
            Write-Host ""
            Write-Host "🎉 Eureka Server is ready! Total startup time: $($elapsedTime.ToString('mm\:ss'))" -ForegroundColor Green
            Write-Host ""
            Write-Host "🌐 Access Eureka Dashboard at: http://localhost:8761" -ForegroundColor Cyan
            break
        }
        
        # Check if we should show logs
        if ($currentCheck -eq 3 -or $currentCheck -eq 6 -or $currentCheck -eq 9) {
            Show-RecentLogs "eureka-server" 10
        }
    } else {
        Write-Host "Container is not running. Checking logs..." -ForegroundColor Red
        Show-RecentLogs "eureka-server" 20
        break
    }
    
    if ($currentCheck -lt $maxChecks) {
        Write-Host "Waiting $checkInterval seconds before next check..." -ForegroundColor Yellow
        Start-Sleep $checkInterval
    }
}

if ($currentCheck -ge $maxChecks) {
    Write-Host ""
    Write-Host "⏰ Timeout reached ($maxWaitMinutes minutes)" -ForegroundColor Red
    Write-Host "Eureka Server is taking longer than expected to start." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "📋 Troubleshooting steps:" -ForegroundColor Cyan
    Write-Host "1. Check full logs: docker-compose logs eureka-server" -ForegroundColor White
    Write-Host "2. Check system resources: docker stats" -ForegroundColor White
    Write-Host "3. Restart container: docker-compose restart eureka-server" -ForegroundColor White
    Write-Host "4. Check port availability: netstat -an | findstr :8761" -ForegroundColor White
    Write-Host ""
    
    Show-RecentLogs "eureka-server" 30
}

Write-Host ""
Write-Host "📊 Quick Status Check:" -ForegroundColor Cyan
Write-Host "docker-compose ps eureka-server" -ForegroundColor White
docker-compose ps eureka-server
