# Library Management System - Microservices Architecture

This repository contains the microservices implementation of the Library Management System, decomposed from the original monolithic application.

## Architecture Overview

The system has been decomposed into the following microservices:

### Core Services
1. **User Service** (Port: 8081) - User management and authentication
2. **Book Service** (Port: 8082) - Book catalog and inventory management
3. **Transaction Service** (Port: 8083) - Borrow/return operations
4. **Analytics Service** (Port: 8084) - Reports and analytics

### Infrastructure Services
5. **Eureka Server** (Port: 8761) - Service discovery
6. **Config Server** (Port: 8888) - Centralized configuration management
7. **API Gateway** (Port: 8080) - API routing and load balancing

### Monitoring Stack
- **Prometheus** (Port: 9090) - Metrics collection
- **Grafana** (Port: 3000) - Metrics visualization
- **Zipkin** (Port: 9411) - Distributed tracing

## Key Features

### Microservices Patterns Implemented
- **Service Discovery**: Eureka Server for dynamic service registration
- **Configuration Management**: Spring Cloud Config for centralized configuration
- **Circuit Breaker**: Resilience4j for fault tolerance
- **API Gateway**: Spring Cloud Gateway for routing and cross-cutting concerns
- **Event-Driven Architecture**: Azure Service Bus for inter-service communication
- **Database per Service**: Each service has its own H2 database
- **Monitoring**: Prometheus + Grafana for metrics, Zipkin for tracing

### Security
- **Shared Authentication**: User Service generates JWT tokens
- **Token Validation**: Other services validate tokens independently
- **Role-Based Access Control**: Admin, Librarian, and User roles

### Inter-Service Communication
- **Synchronous**: REST APIs with Feign clients
- **Asynchronous**: Azure Service Bus for event-driven operations
- **Circuit Breakers**: Resilience4j for handling service failures

## Prerequisites

- Java 22
- Maven 3.8+
- Docker & Docker Compose
- Azure Service Bus (for event-driven features, optional)

## Quick Start

### 1. Build All Services

```powershell
# PowerShell
.\build-all.ps1

# Or manually:
mvn clean install -DskipTests
```

### 2. Start Services

```powershell
# PowerShell
.\start-microservices.ps1

# Or manually:
docker-compose up -d
```

### 3. Verify Services

Check service status:
```powershell
docker-compose ps
```

Access service health endpoints:
- Eureka: http://localhost:8761
- User Service: http://localhost:8081/actuator/health
- Book Service: http://localhost:8082/actuator/health
- Transaction Service: http://localhost:8083/actuator/health
- Analytics Service: http://localhost:8084/actuator/health

## Service Details

### User Service (8081)
**Responsibilities:**
- User registration and authentication
- JWT token generation and validation
- User profile management
- Role-based access control

**Key Endpoints:**
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User authentication
- `POST /api/auth/validate` - Token validation
- `GET /api/users/profile` - Get current user profile
- `GET /api/users` - List all users (Admin/Librarian)

### Book Service (8082)
**Responsibilities:**
- Book catalog management
- Inventory tracking
- Search and categorization
- CSV import functionality

**Key Endpoints:**
- `GET /api/books` - List books
- `POST /api/books` - Add new book
- `PUT /api/books/{id}` - Update book
- `GET /api/books/search` - Search books
- `POST /api/books/upload` - CSV upload

### Transaction Service (8083)
**Responsibilities:**
- Borrow/return operations
- Transaction history
- Overdue management
- Event publishing for analytics

**Key Endpoints:**
- `POST /api/transactions/borrow` - Borrow book
- `POST /api/transactions/return` - Return book
- `GET /api/transactions/user/{userId}` - User transactions
- `GET /api/transactions/overdue` - Overdue transactions

### Analytics Service (8084)
**Responsibilities:**
- Cross-service data aggregation
- Dashboard generation
- Metrics and reporting
- Event consumption from other services

**Key Endpoints:**
- `GET /api/analytics/dashboard` - Analytics dashboard
- `GET /api/analytics/popular-books` - Popular books report
- `GET /api/analytics/user-stats` - User statistics

## Configuration

### Environment Variables

Set the following environment variables:

```bash
# Azure Service Bus (Optional)
AZURE_SERVICEBUS_CONNECTION_STRING=your_connection_string

# Database URLs (automatically configured for Docker)
SPRING_DATASOURCE_URL=jdbc:h2:mem:database_name
```

### Service Configuration

Each service's configuration is managed by the Config Server. Configuration files are located in:
```
config-server/src/main/resources/config/
├── user-service.yml
├── book-service.yml
├── transaction-service.yml
└── analytics-service.yml
```

## Monitoring and Observability

### Metrics
- **Prometheus**: Collects metrics from all services
- **Grafana**: Visualizes metrics with pre-configured dashboards
- Access Grafana at http://localhost:3000 (admin/admin)

### Distributed Tracing
- **Zipkin**: Traces requests across services
- Access Zipkin at http://localhost:9411

### Health Checks
All services expose health endpoints:
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`

## API Gateway

The API Gateway provides:
- **Unified Entry Point**: Single endpoint for all services
- **Load Balancing**: Distributes requests across service instances
- **Security**: Centralized authentication and authorization
- **Rate Limiting**: Controls API usage
- **CORS Handling**: Cross-origin request management

Gateway routes:
- `/user/**` → User Service
- `/book/**` → Book Service
- `/transaction/**` → Transaction Service
- `/analytics/**` → Analytics Service

## Event-Driven Architecture

The system uses Azure Service Bus for asynchronous communication:

### Events Published:
- **User Registration**: When a new user registers
- **Book Operations**: When books are added/updated
- **Transaction Events**: When books are borrowed/returned
- **Analytics Events**: For real-time dashboard updates

### Event Consumers:
- Analytics Service consumes all events for dashboard updates
- Transaction Service consumes book events for inventory updates

## Circuit Breaker

Resilience4j circuit breakers are configured for:
- Service-to-service communication
- External service calls
- Database operations

Configuration in `application.yml`:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      user-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### API Testing
Use the provided Postman collection or test scripts in the `tests/` directory.

## Deployment

### Local Development
Use Docker Compose for local development:
```bash
docker-compose up -d
```

### Production
For production deployment:
1. Build Docker images
2. Push to container registry
3. Deploy to Kubernetes/Docker Swarm
4. Configure external databases
5. Set up proper service mesh (Istio/Linkerd)

## Troubleshooting

### Common Issues

1. **Services not starting**: Check Docker daemon and port availability
2. **Service discovery issues**: Ensure Eureka Server is running first
3. **Configuration issues**: Check Config Server logs
4. **Database issues**: Verify H2 console access at `/h2-console`

### Useful Commands

```bash
# View all service logs
docker-compose logs

# View specific service logs
docker-compose logs user-service

# Restart a service
docker-compose restart user-service

# Scale a service
docker-compose up -d --scale user-service=2

# Stop all services
docker-compose down
```

## Performance Considerations

- **Database**: Consider migrating to dedicated databases in production
- **Caching**: Implement Redis for frequently accessed data
- **Load Balancing**: Use external load balancers for high availability
- **Service Mesh**: Consider Istio for advanced traffic management

## Security Considerations

- **JWT Tokens**: Rotate secret keys regularly
- **HTTPS**: Enable TLS in production
- **Database Security**: Use proper authentication and encryption
- **Service Communication**: Use mTLS for service-to-service communication

## Future Enhancements

- **CQRS Pattern**: Separate read and write models
- **Event Sourcing**: Store events for audit trails
- **Saga Pattern**: Implement distributed transactions
- **GraphQL**: Unified API for frontend applications
- **WebSockets**: Real-time notifications

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes with proper tests
4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
