# PowerShell script to stop all microservice containers

Write-Host "Stopping All Library Management Microservice Containers..." -ForegroundColor Red
Write-Host ""

# List of all container names
$containers = @(
    "analytics-service-container",
    "transaction-service-container", 
    "book-service-container",
    "user-service-container",
    "api-gateway-container",
    "config-server-container",
    "eureka-server-container"
)

# Stop containers in reverse order (opposite of startup)
foreach ($container in $containers) {
    Write-Host "Stopping $container..." -ForegroundColor Yellow
    docker stop $container 2>$null
    docker rm $container 2>$null
    
    # Check if container was running
    $wasRunning = $LASTEXITCODE -eq 0
    if ($wasRunning) {
        Write-Host "✅ $container stopped and removed" -ForegroundColor Green
    } else {
        Write-Host "ℹ️  $container was not running" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "✅ All microservice containers stopped!" -ForegroundColor Green
Write-Host ""
Write-Host "To verify: docker ps" -ForegroundColor Yellow
Write-Host "To see stopped containers: docker ps -a" -ForegroundColor Yellow
