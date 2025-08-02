# Microservices Startup Script with Sample Data
# This script starts all microservices in the correct order and initializes sample data

Write-Host "=== Starting Library Management Microservices ===" -ForegroundColor Green

# Function to check if a service is ready
function Wait-ForService {
    param(
        [string]$ServiceName,
        [string]$Url,
        [int]$MaxAttempts = 30
    )
    
    Write-Host "Waiting for $ServiceName to be ready..." -ForegroundColor Yellow
    
    for ($i = 1; $i -le $MaxAttempts; $i++) {
        try {
            $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "$ServiceName is ready!" -ForegroundColor Green
                return $true
            }
        }
        catch {
            # Service not ready yet
        }
        
        Write-Host "Attempt $i/$MaxAttempts - $ServiceName not ready yet, waiting..." -ForegroundColor Gray
        Start-Sleep -Seconds 5
    }
    
    Write-Host "$ServiceName failed to start within expected time!" -ForegroundColor Red
    return $false
}

# Function to build all services
function Build-Services {
    Write-Host "Building all microservices..." -ForegroundColor Cyan
    
    # Build parent project
    mvn clean compile -DskipTests
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build failed!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Build completed successfully!" -ForegroundColor Green
}

# Stop any existing processes
Write-Host "Stopping any existing microservices..." -ForegroundColor Yellow
Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*spring-boot*" } | Stop-Process -Force
Start-Sleep -Seconds 3

# Build services
Build-Services

# Start services in dependency order
Write-Host "Starting microservices in dependency order..." -ForegroundColor Cyan

# 1. Start Eureka Server (Service Discovery)
Write-Host "1. Starting Eureka Server..." -ForegroundColor Blue
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD\eureka-server`" && mvn spring-boot:run" -WindowStyle Minimized
Wait-ForService -ServiceName "Eureka Server" -Url "http://localhost:8761/actuator/health"

# 2. Start Config Server
Write-Host "2. Starting Config Server..." -ForegroundColor Blue
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD\config-server`" && mvn spring-boot:run" -WindowStyle Minimized
Wait-ForService -ServiceName "Config Server" -Url "http://localhost:8888/actuator/health"

# 3. Start Core Business Services
Write-Host "3. Starting User Service..." -ForegroundColor Blue
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD\user-service`" && mvn spring-boot:run" -WindowStyle Minimized

Write-Host "4. Starting Book Service..." -ForegroundColor Blue
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD\book-service`" && mvn spring-boot:run" -WindowStyle Minimized

# Wait for core services
Wait-ForService -ServiceName "User Service" -Url "http://localhost:8081/actuator/health"
Wait-ForService -ServiceName "Book Service" -Url "http://localhost:8082/actuator/health"

# 4. Start Transaction Service (depends on User and Book services)
Write-Host "5. Starting Transaction Service..." -ForegroundColor Blue
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD\transaction-service`" && mvn spring-boot:run" -WindowStyle Minimized
Wait-ForService -ServiceName "Transaction Service" -Url "http://localhost:8083/actuator/health"

# 5. Start Analytics Service (depends on all other services)
Write-Host "6. Starting Analytics Service..." -ForegroundColor Blue
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD\analytics-service`" && mvn spring-boot:run" -WindowStyle Minimized
Wait-ForService -ServiceName "Analytics Service" -Url "http://localhost:8084/actuator/health"

# 6. Start API Gateway (load balancer)
Write-Host "7. Starting API Gateway..." -ForegroundColor Blue
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD\api-gateway`" && mvn spring-boot:run" -WindowStyle Minimized
Wait-ForService -ServiceName "API Gateway" -Url "http://localhost:8080/actuator/health"

Write-Host "`n=== All Microservices Started Successfully! ===" -ForegroundColor Green

# Display service status
Write-Host "`n=== Service Endpoints ===" -ForegroundColor Cyan
Write-Host "Eureka Server (Service Discovery): http://localhost:8761" -ForegroundColor White
Write-Host "Config Server: http://localhost:8888" -ForegroundColor White
Write-Host "API Gateway (Main Entry Point): http://localhost:8080" -ForegroundColor White
Write-Host "User Service: http://localhost:8081" -ForegroundColor White
Write-Host "Book Service: http://localhost:8082" -ForegroundColor White
Write-Host "Transaction Service: http://localhost:8083" -ForegroundColor White
Write-Host "Analytics Service: http://localhost:8084" -ForegroundColor White

Write-Host "`n=== Sample Data Information ===" -ForegroundColor Cyan
Write-Host "Sample data has been automatically initialized across all services:" -ForegroundColor White
Write-Host "- 48 Users (1 admin, 2 librarians, 45 members)" -ForegroundColor Gray
Write-Host "- 50+ Books across multiple categories" -ForegroundColor Gray
Write-Host "- 85 Transactions over the past 6 months" -ForegroundColor Gray
Write-Host "- Real-time analytics dashboard available" -ForegroundColor Gray

Write-Host "`n=== Testing the System ===" -ForegroundColor Cyan
Write-Host "1. Visit Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "2. Test API Gateway: http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "3. Get Analytics Dashboard: http://localhost:8080/analytics/dashboard" -ForegroundColor White
Write-Host "4. Login with sample user: username='user1', password='password'" -ForegroundColor White

Write-Host "`n=== Sample API Calls ===" -ForegroundColor Cyan
Write-Host "# Login to get JWT token:" -ForegroundColor Gray
Write-Host "POST http://localhost:8080/users/login" -ForegroundColor Gray
Write-Host "Body: {`"username`": `"user1`", `"password`": `"password`"}" -ForegroundColor Gray
Write-Host "" -ForegroundColor Gray
Write-Host "# Get books:" -ForegroundColor Gray
Write-Host "GET http://localhost:8080/books" -ForegroundColor Gray
Write-Host "" -ForegroundColor Gray
Write-Host "# Get analytics dashboard:" -ForegroundColor Gray
Write-Host "GET http://localhost:8080/analytics/dashboard" -ForegroundColor Gray
Write-Host "Header: Authorization: Bearer <JWT_TOKEN>" -ForegroundColor Gray

Write-Host "`nPress Ctrl+C to stop all services" -ForegroundColor Yellow
Write-Host "Microservices are running in background processes..." -ForegroundColor Green

# Keep script running
try {
    while ($true) {
        Start-Sleep -Seconds 10
        
        # Check if all services are still running
        $services = @(
            @{ Name = "Eureka"; Url = "http://localhost:8761/actuator/health" },
            @{ Name = "Config"; Url = "http://localhost:8888/actuator/health" },
            @{ Name = "User"; Url = "http://localhost:8081/actuator/health" },
            @{ Name = "Book"; Url = "http://localhost:8082/actuator/health" },
            @{ Name = "Transaction"; Url = "http://localhost:8083/actuator/health" },
            @{ Name = "Analytics"; Url = "http://localhost:8084/actuator/health" },
            @{ Name = "Gateway"; Url = "http://localhost:8080/actuator/health" }
        )
        
        $allRunning = $true
        foreach ($service in $services) {
            try {
                $response = Invoke-WebRequest -Uri $service.Url -Method GET -TimeoutSec 3 -ErrorAction SilentlyContinue
                if ($response.StatusCode -ne 200) {
                    $allRunning = $false
                    break
                }
            }
            catch {
                $allRunning = $false
                break
            }
        }
        
        if (-not $allRunning) {
            Write-Host "One or more services have stopped. Check the console windows." -ForegroundColor Red
        }
    }
}
finally {
    Write-Host "`nStopping all microservices..." -ForegroundColor Yellow
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*spring-boot*" } | Stop-Process -Force
    Write-Host "All services stopped." -ForegroundColor Green
}
