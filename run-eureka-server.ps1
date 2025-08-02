# PowerShell script to run Eureka Server individually

Write-Host "Starting Eureka Server..." -ForegroundColor Green

# Check if image exists
$imageExists = docker images "library-eureka-server" --format "table {{.Repository}}" | Select-String "library-eureka-server"
if (-not $imageExists) {
    Write-Host "❌ Eureka Server Docker image not found. Please run build-individual.ps1 first." -ForegroundColor Red
    exit 1
}

# Stop existing container if running
Write-Host "Stopping any existing Eureka Server container..." -ForegroundColor Yellow
docker stop eureka-server-container 2>$null
docker rm eureka-server-container 2>$null

# Run Eureka Server
Write-Host "Starting Eureka Server container..." -ForegroundColor Yellow
docker run -d `
    --name eureka-server-container `
    -p 8761:8761 `
    -e EUREKA_CLIENT_REGISTER_WITH_EUREKA=false `
    -e EUREKA_CLIENT_FETCH_REGISTRY=false `
    library-eureka-server

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Eureka Server started successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Eureka Server is starting up..." -ForegroundColor Yellow
    Write-Host "URL: http://localhost:8761" -ForegroundColor Cyan
    Write-Host "Note: It may take 60-90 seconds to be fully ready" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To check logs: docker logs eureka-server-container" -ForegroundColor White
    Write-Host "To stop: docker stop eureka-server-container" -ForegroundColor White
} else {
    Write-Host "❌ Failed to start Eureka Server" -ForegroundColor Red
    exit 1
}
