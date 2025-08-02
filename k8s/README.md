# Library Management System - Kubernetes Deployment

This document provides instructions for deploying the Library Management microservices to a Kubernetes cluster.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │────│  Config Server  │────│  Eureka Server  │
│    (Port 8080)  │    │   (Port 8888)   │    │   (Port 8761)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │
    ┌────┴────┬───────────┬───────────────┐
    │         │           │               │
┌───▼───┐ ┌──▼────┐ ┌────▼─────┐ ┌──────▼────┐
│ User  │ │ Book  │ │Transaction│ │ Analytics │
│Service│ │Service│ │ Service   │ │  Service  │
│ 8081  │ │ 8082  │ │   8083    │ │   8084    │
└───────┘ └───────┘ └───────────┘ └───────────┘
```

## 🚀 Quick Start

### Prerequisites
- Kubernetes cluster (minikube, Docker Desktop, or cloud provider)
- kubectl configured
- Docker Desktop running
- Java 22 and Maven installed

### 1. Build and Deploy
```powershell
# Build JARs and Docker images, then deploy to K8s
.\deploy-k8s.ps1
```

### 2. Check Status
```powershell
# View all services status
.\manage-k8s.ps1 -Action status
```

### 3. Access Services
```powershell
# Port forward API Gateway (main entry point)
.\manage-k8s.ps1 -Action port-forward -Service api-gateway -Port 8080

# Access at: http://localhost:8080
```

## 📋 Available Services

| Service | Port | Replicas | Purpose |
|---------|------|----------|---------|
| Config Server | 8888 | 1 | Centralized configuration |
| Eureka Server | 8761 | 1 | Service discovery |
| User Service | 8081 | 2 | Authentication & user management |
| Book Service | 8082 | 2 | Book catalog management |
| Transaction Service | 8083 | 2 | Borrowing/returning logic |
| Analytics Service | 8084 | 1 | Dashboard and reports |
| API Gateway | 8080 | 2 | Request routing & load balancing |

## 🛠️ Management Commands

### View Logs
```powershell
.\manage-k8s.ps1 -Action logs -Service config-server
.\manage-k8s.ps1 -Action logs -Service user-service
```

### Restart Services
```powershell
.\manage-k8s.ps1 -Action restart -Service config-server
```

### Scale Services
```powershell
# Scale user service to 3 replicas
.\manage-k8s.ps1 -Action scale -Service user-service -Replicas 3
```

### Port Forward Services
```powershell
# API Gateway (main entry)
.\manage-k8s.ps1 -Action port-forward -Service api-gateway -Port 8080

# Eureka Dashboard
.\manage-k8s.ps1 -Action port-forward -Service eureka-server -Port 8761

# Direct service access
.\manage-k8s.ps1 -Action port-forward -Service user-service -Port 8081
```

## 🔍 Inter-Service Communication

Services communicate via Kubernetes DNS:
```
http://user-service.library-management.svc.cluster.local:8081
http://book-service.library-management.svc.cluster.local:8082
http://transaction-service.library-management.svc.cluster.local:8083
http://analytics-service.library-management.svc.cluster.local:8084
```

## 📊 API Endpoints

### Via API Gateway (http://localhost:8080)
```
# Authentication
POST /users/login
POST /users/register

# Books
GET /books
POST /books
GET /books/{id}

# Transactions
GET /transactions
POST /transactions/borrow
POST /transactions/return

# Analytics
GET /analytics/dashboard
GET /analytics/reports
```

### Direct Service Access
```
# User Service
http://localhost:8081/users/

# Book Service  
http://localhost:8082/books/

# Transaction Service
http://localhost:8083/transactions/

# Analytics Service
http://localhost:8084/analytics/
```

## 🐛 Troubleshooting

### Check Pod Status
```bash
kubectl get pods -n library-management
kubectl describe pod <pod-name> -n library-management
```

### View Service Logs
```bash
kubectl logs -f deployment/config-server -n library-management
kubectl logs -f deployment/user-service -n library-management
```

### Common Issues

1. **Config Server not starting**
   ```bash
   kubectl logs deployment/config-server -n library-management
   ```

2. **Services can't connect to Config Server**
   - Check if Config Server is running: `kubectl get pods -n library-management`
   - Verify init containers completed successfully

3. **Inter-service communication fails**
   - Check service DNS: `kubectl get services -n library-management`
   - Verify environment variables in deployment

### Manual Commands
```bash
# Get all resources
kubectl get all -n library-management

# Delete specific deployment
kubectl delete deployment user-service -n library-management

# Apply single manifest
kubectl apply -f k8s/03-user-service.yaml

# Port forward manually
kubectl port-forward service/api-gateway 8080:8080 -n library-management
```

## 🧹 Cleanup

### Delete All Services
```powershell
.\manage-k8s.ps1 -Action delete
```

### Or manually:
```bash
kubectl delete namespace library-management
```

## 📈 Monitoring

Deploy Prometheus monitoring:
```bash
kubectl apply -f k8s/08-monitoring.yaml
```

Access Prometheus:
```bash
kubectl port-forward service/prometheus 9090:9090 -n library-management
# Open: http://localhost:9090
```

## 🌐 Production Considerations

1. **Resource Limits**: Adjust CPU/memory limits in deployment manifests
2. **Persistent Storage**: Add persistent volumes for data storage
3. **Secrets Management**: Use Kubernetes secrets for sensitive data
4. **Ingress**: Configure ingress controller for external access
5. **Health Checks**: Fine-tune readiness/liveness probes
6. **Monitoring**: Set up comprehensive monitoring and alerting
7. **Security**: Implement RBAC and network policies

## 📚 Sample Data

Each service initializes with sample data:
- **Users**: 48 test users (user1/password, admin/admin)
- **Books**: 50+ books across multiple categories  
- **Transactions**: 85 realistic borrowing/return transactions
- **Analytics**: Generated from the above data

Test credentials:
- User: `user1` / `password`
- Admin: `admin` / `admin`
