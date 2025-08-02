# PowerShell script to build all microservices individually

Write-Host "Building Library Management Microservices Individually..." -ForegroundColor Green
Write-Host ""

# Function to build a service
function Build-Service {
    param(
        [string]$ServiceName,
        [string]$ServicePath
    )
    
    Write-Host "Building $ServiceName..." -ForegroundColor Yellow
    
    # Build the Maven project first
    Set-Location $ServicePath
    mvn clean package -DskipTests
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Failed to build $ServiceName JAR" -ForegroundColor Red
        return $false
    }
    
    # Build Docker image
    docker build -t "library-$ServiceName" .
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Failed to build $ServiceName Docker image" -ForegroundColor Red
        return $false
    }
    
    Write-Host "✅ $ServiceName built successfully!" -ForegroundColor Green
    Write-Host ""
    return $true
}

# Store original location
$OriginalLocation = Get-Location
$BaseDir = "c:\Users\gurud\Documents\SEM8\Application\Project\microservices"

try {
    # Build all services in dependency order
    $services = @(
        @{Name="eureka-server"; Path="$BaseDir\eureka-server"},
        @{Name="config-server"; Path="$BaseDir\config-server"},
        @{Name="user-service"; Path="$BaseDir\user-service"},
        @{Name="book-service"; Path="$BaseDir\book-service"},
        @{Name="transaction-service"; Path="$BaseDir\transaction-service"},
        @{Name="analytics-service"; Path="$BaseDir\analytics-service"},
        @{Name="api-gateway"; Path="$BaseDir\api-gateway"}
    )
    
    $allSuccess = $true
    foreach ($service in $services) {
        $success = Build-Service -ServiceName $service.Name -ServicePath $service.Path
        if (-not $success) {
            $allSuccess = $false
            break
        }
    }
    
    if ($allSuccess) {
        Write-Host "🎉 All microservices built successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Built Docker images:" -ForegroundColor Cyan
        docker images | Select-String "library-"
    } else {
        Write-Host "❌ Build process failed. Please check the errors above." -ForegroundColor Red
        exit 1
    }
    
} finally {
    # Return to original location
    Set-Location $OriginalLocation
}
