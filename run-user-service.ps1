# PowerShell script to run User Service individually

Write-Host "Starting User Service..." -ForegroundColor Green

# Check if image exists
$imageExists = docker images "library-user-service" --format "table {{.Repository}}" | Select-String "library-user-service"
if (-not $imageExists) {
    Write-Host "❌ User Service Docker image not found. Please run build-individual.ps1 first." -ForegroundColor Red
    exit 1
}

# Check dependencies
$eurekaRunning = docker ps --filter "name=eureka-server-container" --format "table {{.Names}}" | Select-String "eureka-server-container"
$configRunning = docker ps --filter "name=config-server-container" --format "table {{.Names}}" | Select-String "config-server-container"

if (-not $eurekaRunning) {
    Write-Host "⚠️  Eureka Server is not running. Please start it first with run-eureka-server.ps1" -ForegroundColor Yellow
}

if (-not $configRunning) {
    Write-Host "⚠️  Config Server is not running. Please start it first with run-config-server.ps1" -ForegroundColor Yellow
}

# Stop existing container if running
Write-Host "Stopping any existing User Service container..." -ForegroundColor Yellow
docker stop user-service-container 2>$null
docker rm user-service-container 2>$null

# Run User Service
Write-Host "Starting User Service container..." -ForegroundColor Yellow
docker run -d `
    --name user-service-container `
    -p 8081:8081 `
    -e SPRING_CLOUD_CONFIG_URI=http://host.docker.internal:8888 `
    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://host.docker.internal:8761/eureka/ `
    -e SPRING_DATASOURCE_URL=jdbc:h2:mem:userdb `
    library-user-service

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ User Service started successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "User Service is starting up..." -ForegroundColor Yellow
    Write-Host "URL: http://localhost:8081" -ForegroundColor Cyan
    Write-Host "Health Check: http://localhost:8081/actuator/health" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To check logs: docker logs user-service-container" -ForegroundColor White
    Write-Host "To stop: docker stop user-service-container" -ForegroundColor White
} else {
    Write-Host "❌ Failed to start User Service" -ForegroundColor Red
    exit 1
}
