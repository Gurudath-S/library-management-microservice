# PowerShell script to run API Gateway individually

Write-Host "Starting API Gateway..." -ForegroundColor Green

# Check if image exists
$imageExists = docker images "library-api-gateway" --format "table {{.Repository}}" | Select-String "library-api-gateway"
if (-not $imageExists) {
    Write-Host "❌ API Gateway Docker image not found. Please run build-individual.ps1 first." -ForegroundColor Red
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

if (-not $eurekaRunning -or -not $configRunning) {
    Write-Host "API Gateway requires both Eureka Server and Config Server to be running." -ForegroundColor Red
    exit 1
}

# Stop existing container if running
Write-Host "Stopping any existing API Gateway container..." -ForegroundColor Yellow
docker stop api-gateway-container 2>$null
docker rm api-gateway-container 2>$null

# Run API Gateway
Write-Host "Starting API Gateway container..." -ForegroundColor Yellow
docker run -d `
    --name api-gateway-container `
    -p 8080:8080 `
    -e SPRING_CLOUD_CONFIG_URI=http://host.docker.internal:8888 `
    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://host.docker.internal:8761/eureka/ `
    library-api-gateway

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ API Gateway started successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "API Gateway is starting up..." -ForegroundColor Yellow
    Write-Host "URL: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "Health Check: http://localhost:8080/actuator/health" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To check logs: docker logs api-gateway-container" -ForegroundColor White
    Write-Host "To stop: docker stop api-gateway-container" -ForegroundColor White
} else {
    Write-Host "❌ Failed to start API Gateway" -ForegroundColor Red
    exit 1
}
