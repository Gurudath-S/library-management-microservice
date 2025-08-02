# Kubernetes Service Management Script

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("status", "logs", "restart", "scale", "delete", "port-forward")]
    [string]$Action,
    
    [string]$Service = "",
    [int]$Replicas = 1,
    [int]$Port = 8080
)

$namespace = "library-management"

switch ($Action) {
    "status" {
        Write-Host "📊 Service Status:" -ForegroundColor Cyan
        kubectl get pods -n $namespace
        Write-Host ""
        kubectl get services -n $namespace
        Write-Host ""
        kubectl get deployments -n $namespace
    }
    
    "logs" {
        if ($Service -eq "") {
            Write-Host "❌ Please specify a service name" -ForegroundColor Red
            Write-Host "Available services: config-server, eureka-server, user-service, book-service, transaction-service, analytics-service, api-gateway" -ForegroundColor Yellow
            exit 1
        }
        Write-Host "📝 Logs for $Service:" -ForegroundColor Cyan
        kubectl logs -f deployment/$Service -n $namespace
    }
    
    "restart" {
        if ($Service -eq "") {
            Write-Host "❌ Please specify a service name" -ForegroundColor Red
            exit 1
        }
        Write-Host "🔄 Restarting $Service..." -ForegroundColor Yellow
        kubectl rollout restart deployment/$Service -n $namespace
        kubectl rollout status deployment/$Service -n $namespace
    }
    
    "scale" {
        if ($Service -eq "") {
            Write-Host "❌ Please specify a service name" -ForegroundColor Red
            exit 1
        }
        Write-Host "📈 Scaling $Service to $Replicas replicas..." -ForegroundColor Yellow
        kubectl scale deployment/$Service --replicas=$Replicas -n $namespace
        kubectl get pods -n $namespace -l app=$Service
    }
    
    "delete" {
        Write-Host "🗑️ Deleting all services..." -ForegroundColor Red
        $confirm = Read-Host "Are you sure? (yes/no)"
        if ($confirm -eq "yes") {
            kubectl delete namespace $namespace
            Write-Host "✅ All services deleted" -ForegroundColor Green
        } else {
            Write-Host "❌ Deletion cancelled" -ForegroundColor Yellow
        }
    }
    
    "port-forward" {
        if ($Service -eq "") {
            $Service = "api-gateway"
            $Port = 8080
        }
        Write-Host "🌐 Port forwarding $Service`:$Port..." -ForegroundColor Cyan
        Write-Host "Access at: http://localhost:$Port" -ForegroundColor Green
        kubectl port-forward service/$Service $Port`:$Port -n $namespace
    }
}

# Usage examples
Write-Host ""
Write-Host "Usage Examples:" -ForegroundColor Cyan
Write-Host ".\manage-k8s.ps1 -Action status" -ForegroundColor White
Write-Host ".\manage-k8s.ps1 -Action logs -Service user-service" -ForegroundColor White
Write-Host ".\manage-k8s.ps1 -Action restart -Service config-server" -ForegroundColor White
Write-Host ".\manage-k8s.ps1 -Action scale -Service user-service -Replicas 3" -ForegroundColor White
Write-Host ".\manage-k8s.ps1 -Action port-forward -Service api-gateway -Port 8080" -ForegroundColor White
