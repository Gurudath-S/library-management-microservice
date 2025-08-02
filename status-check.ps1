# PowerShell script to check the status of all microservice containers

Write-Host "Library Management Microservices Status" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Green
Write-Host ""

# List of all containers with their expected ports
$services = @(
    @{Name="eureka-server-container"; Port="8761"; Service="Eureka Server"},
    @{Name="config-server-container"; Port="8888"; Service="Config Server"},
    @{Name="api-gateway-container"; Port="8080"; Service="API Gateway"},
    @{Name="user-service-container"; Port="8081"; Service="User Service"},
    @{Name="book-service-container"; Port="8082"; Service="Book Service"},
    @{Name="transaction-service-container"; Port="8083"; Service="Transaction Service"},
    @{Name="analytics-service-container"; Port="8084"; Service="Analytics Service"}
)

# Check Docker containers
Write-Host "Container Status:" -ForegroundColor Cyan
Write-Host "=================" -ForegroundColor Cyan
foreach ($service in $services) {
    $containerInfo = docker ps --filter "name=$($service.Name)" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String $service.Name
    
    if ($containerInfo) {
        Write-Host "✅ $($service.Service) - Running" -ForegroundColor Green
        Write-Host "   Container: $($service.Name)" -ForegroundColor Gray
        Write-Host "   Status: $($containerInfo.ToString().Split("`t")[1])" -ForegroundColor Gray
    } else {
        Write-Host "❌ $($service.Service) - Not Running" -ForegroundColor Red
        Write-Host "   Container: $($service.Name)" -ForegroundColor Gray
    }
    Write-Host ""
}

Write-Host ""
Write-Host "Health Check URLs:" -ForegroundColor Cyan
Write-Host "==================" -ForegroundColor Cyan
foreach ($service in $services) {
    $containerRunning = docker ps --filter "name=$($service.Name)" --format "table {{.Names}}" | Select-String $service.Name
    
    if ($containerRunning) {
        $healthUrl = "http://localhost:$($service.Port)/actuator/health"
        Write-Host "- $($service.Service): $healthUrl" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "Quick Commands:" -ForegroundColor Cyan
Write-Host "===============" -ForegroundColor Cyan
Write-Host "Start all: .\start-all-individual.ps1" -ForegroundColor White
Write-Host "Stop all: .\stop-all-services.ps1" -ForegroundColor White
Write-Host "Build all: .\build-individual.ps1" -ForegroundColor White
Write-Host "Docker containers: docker ps" -ForegroundColor White
Write-Host "Service logs: docker logs [container-name]" -ForegroundColor White
