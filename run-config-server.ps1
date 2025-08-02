# PowerShell script to run Config Server individually

Write-Host "Starting Config Server..." -ForegroundColor Green

# Check if image exists
$imageExists = docker images "library-config-server" --format "table {{.Repository}}" | Select-String "library-config-server"
if (-not $imageExists) {
    Write-Host "❌ Config Server Docker image not found. Please run build-individual.ps1 first." -ForegroundColor Red
    exit 1
}

# Check if Eureka Server is running
$eurekaRunning = docker ps --filter "name=eureka-server-container" --format "table {{.Names}}" | Select-String "eureka-server-container"
if (-not $eurekaRunning) {
    Write-Host "⚠️  Eureka Server is not running. Please start it first with run-eureka-server.ps1" -ForegroundColor Yellow
    Write-Host "Config Server will try to connect to Eureka, but may have issues." -ForegroundColor Yellow
}

# Stop existing container if running
Write-Host "Stopping any existing Config Server container..." -ForegroundColor Yellow
docker stop config-server-container 2>$null
docker rm config-server-container 2>$null

# Run Config Server
Write-Host "Starting Config Server container..." -ForegroundColor Yellow
docker run -d `
    --name config-server-container `
    -p 8888:8888 `
    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://host.docker.internal:8761/eureka/ `
    library-config-server

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Config Server started successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Config Server is starting up..." -ForegroundColor Yellow
    Write-Host "URL: http://localhost:8888" -ForegroundColor Cyan
    Write-Host "Health Check: http://localhost:8888/actuator/health" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To check logs: docker logs config-server-container" -ForegroundColor White
    Write-Host "To stop: docker stop config-server-container" -ForegroundColor White
} else {
    Write-Host "❌ Failed to start Config Server" -ForegroundColor Red
    exit 1
}
