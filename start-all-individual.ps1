# PowerShell script to start all microservices individually in the correct sequence

Write-Host "Starting All Library Management Microservices Individually..." -ForegroundColor Green
Write-Host "Following sequence: Eureka Server → Config Server → API Gateway → Other Services" -ForegroundColor Cyan
Write-Host ""

# Function to wait for service health
function Wait-ForService {
    param(
        [string]$ServiceName,
        [string]$HealthUrl,
        [int]$MaxAttempts = 20,
        [int]$IntervalSeconds = 6
    )
    
    Write-Host "Waiting for $ServiceName to be ready..." -ForegroundColor Yellow
    $attempts = 0
    $ready = $false
    
    while (-not $ready -and $attempts -lt $MaxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri $HealthUrl -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "✅ $ServiceName is ready!" -ForegroundColor Green
                $ready = $true
            }
        } catch {
            Write-Host "." -NoNewline -ForegroundColor Yellow
            Start-Sleep $IntervalSeconds
            $attempts++
        }
    }
    
    if (-not $ready) {
        Write-Host ""
        Write-Host "❌ $ServiceName failed to become ready within $(($MaxAttempts * $IntervalSeconds) / 60) minutes" -ForegroundColor Red
        return $false
    }
    
    Write-Host ""
    return $true
}

# Step 1: Start Eureka Server
Write-Host "Step 1: Starting Eureka Server..." -ForegroundColor Yellow
& ".\run-eureka-server.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start Eureka Server" -ForegroundColor Red
    exit 1
}

# Wait for Eureka to be ready
if (-not (Wait-ForService -ServiceName "Eureka Server" -HealthUrl "http://localhost:8761/actuator/health" -MaxAttempts 20)) {
    exit 1
}

# Step 2: Start Config Server
Write-Host "Step 2: Starting Config Server..." -ForegroundColor Yellow
& ".\run-config-server.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start Config Server" -ForegroundColor Red
    exit 1
}

# Wait for Config Server to be ready
if (-not (Wait-ForService -ServiceName "Config Server" -HealthUrl "http://localhost:8888/actuator/health" -MaxAttempts 15)) {
    exit 1
}

# Step 3: Start API Gateway
Write-Host "Step 3: Starting API Gateway..." -ForegroundColor Yellow
& ".\run-api-gateway.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start API Gateway" -ForegroundColor Red
    exit 1
}

# Step 4: Start Core Business Services
Write-Host "Step 4: Starting Core Business Services..." -ForegroundColor Yellow

Write-Host "Starting User Service..." -ForegroundColor Cyan
& ".\run-user-service.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start User Service" -ForegroundColor Red
    exit 1
}

Write-Host "Starting Book Service..." -ForegroundColor Cyan
& ".\run-book-service.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start Book Service" -ForegroundColor Red
    exit 1
}

Write-Host "Starting Transaction Service..." -ForegroundColor Cyan
& ".\run-transaction-service.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start Transaction Service" -ForegroundColor Red
    exit 1
}

Write-Host "Starting Analytics Service..." -ForegroundColor Cyan
& ".\run-analytics-service.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start Analytics Service" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎉 All microservices started successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Service URLs:" -ForegroundColor Cyan
Write-Host "- Eureka Server: http://localhost:8761" -ForegroundColor White
Write-Host "- Config Server: http://localhost:8888" -ForegroundColor White
Write-Host "- API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "- User Service: http://localhost:8081" -ForegroundColor White
Write-Host "- Book Service: http://localhost:8082" -ForegroundColor White
Write-Host "- Transaction Service: http://localhost:8083" -ForegroundColor White
Write-Host "- Analytics Service: http://localhost:8084" -ForegroundColor White
Write-Host ""
Write-Host "To check running containers: docker ps" -ForegroundColor Yellow
Write-Host "To view logs: docker logs [container-name]" -ForegroundColor Yellow
Write-Host "To stop all: .\stop-all-services.ps1" -ForegroundColor Yellow
