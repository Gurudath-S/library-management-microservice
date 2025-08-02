# Test Docker Compose Healthchecks

Write-Host "🏥 Testing Docker Compose Healthchecks" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

# Function to check container health
function Check-ContainerHealth {
    param($serviceName)
    
    $healthStatus = docker-compose ps --services --filter health=healthy | Where-Object { $_ -eq $serviceName }
    $runningStatus = docker-compose ps --services --filter status=running | Where-Object { $_ -eq $serviceName }
    
    if ($healthStatus) {
        Write-Host "✅ $serviceName - HEALTHY" -ForegroundColor Green
        return "healthy"
    } elseif ($runningStatus) {
        Write-Host "🟡 $serviceName - RUNNING (health check pending)" -ForegroundColor Yellow
        return "running"
    } else {
        Write-Host "❌ $serviceName - NOT RUNNING" -ForegroundColor Red
        return "stopped"
    }
}

# Rebuild images with wget
Write-Host "Step 1: Rebuilding images with wget support..." -ForegroundColor Yellow
Write-Host "This may take a few minutes..." -ForegroundColor Cyan

$imagesToRebuild = @("eureka-server", "config-server", "user-service")
foreach ($image in $imagesToRebuild) {
    Write-Host "Rebuilding $image..." -ForegroundColor White
    docker-compose build $image --no-cache
}

Write-Host "✅ Images rebuilt" -ForegroundColor Green
Write-Host ""

# Start services
Write-Host "Step 2: Starting services with healthchecks..." -ForegroundColor Yellow

# Start Eureka first
Write-Host "Starting Eureka Server..." -ForegroundColor White
docker-compose up -d eureka-server

# Wait and check Eureka health
$maxWait = 120
$waited = 0
$interval = 10

Write-Host "Waiting for Eureka healthcheck..." -ForegroundColor Cyan
while ($waited -lt $maxWait) {
    Start-Sleep $interval
    $waited += $interval
    
    $status = Check-ContainerHealth "eureka-server"
    if ($status -eq "healthy") {
        break
    }
    
    Write-Host "Waited $waited seconds..." -ForegroundColor Yellow
}

if ($waited -ge $maxWait) {
    Write-Host "❌ Eureka failed to become healthy within $maxWait seconds" -ForegroundColor Red
    Write-Host "Checking logs..." -ForegroundColor Yellow
    docker-compose logs --tail 20 eureka-server
    exit 1
}

# Start Config Server
Write-Host ""
Write-Host "Starting Config Server..." -ForegroundColor White
docker-compose up -d config-server

# Monitor overall health
Write-Host ""
Write-Host "Step 3: Monitoring service health..." -ForegroundColor Yellow

for ($i = 1; $i -le 12; $i++) {
    Write-Host ""
    Write-Host "Health Check Round $i:" -ForegroundColor Cyan
    
    Check-ContainerHealth "eureka-server"
    Check-ContainerHealth "config-server"
    
    if ($i -lt 12) {
        Write-Host "Waiting 15 seconds..." -ForegroundColor Yellow
        Start-Sleep 15
    }
}

Write-Host ""
Write-Host "📊 Final Status:" -ForegroundColor Cyan
Write-Host "docker-compose ps" -ForegroundColor White
docker-compose ps

Write-Host ""
Write-Host "🔍 Health Status Details:" -ForegroundColor Cyan
docker inspect --format='{{.Name}} - {{.State.Health.Status}}' $(docker-compose ps -q) 2>$null

Write-Host ""
Write-Host "If services are healthy, you can now start the remaining services:" -ForegroundColor Green
Write-Host "docker-compose up -d" -ForegroundColor White
