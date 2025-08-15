# PowerShell script to run Transaction Service individually

Write-Host "Starting Transaction Service..." -ForegroundColor Green

# Check if image exists
$imageExists = docker images "library-transaction-service" --format "table {{.Repository}}" | Select-String "library-transaction-service"
if (-not $imageExists) {
    Write-Host "❌ Transaction Service Docker image not found. Please run build-individual.ps1 first." -ForegroundColor Red
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
Write-Host "Stopping any existing Transaction Service container..." -ForegroundColor Yellow
docker stop transaction-service-container 2>$null
docker rm transaction-service-container 2>$null

# Run Transaction Service
Write-Host "Starting Transaction Service container..." -ForegroundColor Yellow
docker run -d `
    --name transaction-service-container `
    -p 8083:8083 `
    -e SPRING_CLOUD_CONFIG_URI=http://host.docker.internal:8888 `
    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://host.docker.internal:8761/eureka/ `
    -e SPRING_DATASOURCE_URL=jdbc:h2:mem:transactiondb `
    -e AZURE_SERVICEBUS_CONNECTION_STRING="Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=dummy" `
    library-transaction-service

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Transaction Service started successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Transaction Service is starting up..." -ForegroundColor Yellow
    Write-Host "URL: http://localhost:8083" -ForegroundColor Cyan
    Write-Host "Health Check: http://localhost:8083/actuator/health" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To check logs: docker logs transaction-service-container" -ForegroundColor White
    Write-Host "To stop: docker stop transaction-service-container" -ForegroundColor White
} else {
    Write-Host "❌ Failed to start Transaction Service" -ForegroundColor Red
    exit 1
}
