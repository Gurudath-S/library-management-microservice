# Update all Dockerfiles to include wget for healthchecks

$services = @("config-server", "user-service", "book-service", "transaction-service", "analytics-service", "api-gateway")

foreach ($service in $services) {
    $dockerfilePath = "$service\Dockerfile"
    
    if (Test-Path $dockerfilePath) {
        Write-Host "Updating Dockerfile for $service..." -ForegroundColor Yellow
        
        # Read the current Dockerfile
        $content = Get-Content $dockerfilePath -Raw
        
        # Check if wget is already installed
        if ($content -notmatch "wget") {
            # Replace the FROM line to include wget installation
            $updatedContent = $content -replace 
                "FROM openjdk:22-jdk-slim",
                @"
FROM openjdk:22-jdk-slim

# Install wget for healthchecks
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*
"@
            
            # Write the updated content back
            $updatedContent | Set-Content $dockerfilePath -Encoding UTF8
            Write-Host "✅ Updated $service Dockerfile" -ForegroundColor Green
        } else {
            Write-Host "✅ $service Dockerfile already has wget" -ForegroundColor Green
        }
    } else {
        Write-Host "❌ Dockerfile not found for $service" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "🔄 Now you need to rebuild the Docker images:" -ForegroundColor Cyan
Write-Host "docker-compose build" -ForegroundColor White
Write-Host ""
Write-Host "Or rebuild individual services:" -ForegroundColor Cyan
foreach ($service in $services) {
    Write-Host "docker-compose build $service" -ForegroundColor White
}
