# PowerShell script to build all microservices

Write-Host "Building Library Management Microservices..." -ForegroundColor Green

# Build all services
Write-Host "Building parent project..." -ForegroundColor Yellow
mvn clean install -DskipTests

Write-Host "Building Eureka Server..." -ForegroundColor Yellow
Set-Location eureka-server
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building Config Server..." -ForegroundColor Yellow
Set-Location config-server
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building User Service..." -ForegroundColor Yellow
Set-Location user-service
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building Book Service..." -ForegroundColor Yellow
Set-Location book-service
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building Transaction Service..." -ForegroundColor Yellow
Set-Location transaction-service
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building Analytics Service..." -ForegroundColor Yellow
Set-Location analytics-service
mvn clean package -DskipTests
Set-Location ..

Write-Host "Building API Gateway..." -ForegroundColor Yellow
Set-Location api-gateway
mvn clean package -DskipTests
Set-Location ..

Write-Host "All services built successfully!" -ForegroundColor Green
Write-Host "You can now run: docker-compose up -d" -ForegroundColor Cyan
