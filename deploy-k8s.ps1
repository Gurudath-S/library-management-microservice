# Build and Deploy Library Management Microservices to Kubernetes

Write-Host "Building Library Management Microservices for Kubernetes..." -ForegroundColor Green

# Step 1: Build all JAR files
Write-Host "Step 1: Building JAR files..." -ForegroundColor Yellow
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven build failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ JAR files built successfully" -ForegroundColor Green

# Step 2: Build Docker images
Write-Host "Step 2: Building Docker images..." -ForegroundColor Yellow

$services = @("eureka-server", "config-server", "user-service", "book-service", "transaction-service", "analytics-service", "api-gateway")

foreach ($service in $services) {
    Write-Host "Building Docker image for $service..." -ForegroundColor Cyan
    
    Set-Location $service
    
    # Verify JAR exists
    if (!(Test-Path "target/$service-1.0.0.jar")) {
        Write-Host "❌ JAR file not found for $service" -ForegroundColor Red
        exit 1
    }
    
    # Build Docker image
    docker build -t "library-$service`:latest" .
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Docker image built for $service" -ForegroundColor Green
    } else {
        Write-Host "❌ Docker image build failed for $service" -ForegroundColor Red
        exit 1
    }
    
    Set-Location ..
}

# Step 3: Deploy to Kubernetes
Write-Host "Step 3: Deploying to Kubernetes..." -ForegroundColor Yellow

# Apply Kubernetes manifests in order
$manifests = @(
    "k8s/00-namespace-config.yaml",
    "k8s/01-config-server.yaml",
    "k8s/02-eureka-server.yaml",
    "k8s/03-user-service.yaml",
    "k8s/04-book-service.yaml",
    "k8s/05-transaction-service.yaml",
    "k8s/06-analytics-service.yaml",
    "k8s/07-api-gateway.yaml"
)

foreach ($manifest in $manifests) {
    Write-Host "Applying $manifest..." -ForegroundColor Cyan
    kubectl apply -f $manifest
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Failed to apply $manifest" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✅ All manifests applied successfully" -ForegroundColor Green

# Step 4: Wait for services to be ready
Write-Host "Step 4: Waiting for services to be ready..." -ForegroundColor Yellow

Write-Host "Checking pod status..." -ForegroundColor Cyan
kubectl get pods -n library-management -w --timeout=300s

Write-Host ""
Write-Host "🎉 Deployment Complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Useful Commands:" -ForegroundColor Cyan
Write-Host "kubectl get pods -n library-management" -ForegroundColor White
Write-Host "kubectl get services -n library-management" -ForegroundColor White
Write-Host "kubectl logs -f deployment/config-server -n library-management" -ForegroundColor White
Write-Host "kubectl port-forward service/api-gateway 8080:8080 -n library-management" -ForegroundColor White
Write-Host ""
Write-Host "Service URLs (after port-forward):" -ForegroundColor Cyan
Write-Host "- API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "- Eureka: kubectl port-forward service/eureka-server 8761:8761 -n library-management" -ForegroundColor White
Write-Host ""
Write-Host "To delete: kubectl delete namespace library-management" -ForegroundColor Yellow
