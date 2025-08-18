Library Management System - Microservices Architecture
Distributed system implementation using Spring Cloud microservices pattern with service discovery and API gateway.

🏗️ Architecture Overview
Architectural Pattern: Microservices with Service Discovery

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


Core Business Domains
  -User Management & Authentication
  -Book Catalog & Inventory
  -Transaction Processing
  -Analytics & Reporting
  -Configuration Management
🛠️ Technology Stack
Core Framework
  -Spring Boot 3.3.0
  -Java 22
  -H2 Database (in-memory per service)
  -Maven
Key Libraries/Modules
  -Spring Cloud Gateway - API routing and load balancing layer
  -Spring Cloud Eureka - Service discovery and registration
  -Spring Cloud Config - Centralized configuration management
  -Spring Cloud OpenFeign - Inter-service communication
  -Resilience4j - Circuit breaker and fault tolerance
  -Spring Security JWT - Authentication and authorization

📁 Component Architecture
    microservices-root/
    ├── infrastructure/
    │   ├── config-server/     # Configuration management
    │   ├── eureka-server/     # Service registry
    │   └── api-gateway/       # Request routing
    ├── core-services/
    │   ├── user-service/      # Authentication domain
    │   ├── book-service/      # Catalog domain
    │   ├── transaction-service/  # Business logic domain
    │   └── analytics-service/ # Reporting domain
    ├── monitoring/
    │   ├── prometheus/        # Metrics collection
    │   ├── grafana/          # Visualization layer
    │   └── zipkin/           # Distributed tracing
    └── deployment/
        ├── docker-compose.yml # Container orchestration
        └── k8s-local/        # Kubernetes manifests


🔄 Component Interactions
  Primary Flow
    Client Request → API Gateway → Target Service → Database
    Service Discovery: All services register with Eureka Server
    Configuration: Services pull config from Config Server on startup
    Inter-service: Services communicate via Feign clients with circuit breakers
  Data Flow Pattern
    Request flow: API Gateway → Service Discovery → Business Service → Data Layer
    Response flow: Data Layer → Business Logic → Response Aggregation → Client
    Event flow: Service → Event Bus → Consuming Services (async)

🎯 Key Architectural Patterns
  Design Patterns Implemented
    Circuit Breaker - Resilience4j for fault tolerance
    Service Registry - Eureka for dynamic service discovery
    API Gateway - Single entry point with routing
    Database per Service - Data isolation principle
    Event-Driven Communication - Asynchronous messaging
    Configuration Externalization - Spring Cloud Config
  Framework-Specific Patterns
    Spring Cloud Contract - API contract testing
    Spring Boot Actuator - Health checks and metrics
    Feign Declarative Clients - REST client abstraction
    Spring Security JWT - Token-based authentication

🔐 Security Architecture
  Authentication Components
    JWT Token Generation - User Service creates tokens
    Token Validation - Each service validates independently
    Shared Security Context - Token propagation across services
  Authorization Layers
    Role-Based Access Control - Admin, Librarian, User roles
    Method-Level Security - @PreAuthorize annotations
    Gateway Security - Centralized authentication at API Gateway

📊 Module Integration
  Service Dependencies
  API Gateway → All Services (routing)
  Config Server → All Services (configuration)
  Eureka Server ← All Services (registration)
  Analytics Service → User Service, Book Service, Transaction Service (data aggregation)
  Transaction Service → User Service, Book Service (validation)

Database Independence
  Each service maintains its own H2 database instance
  No shared database access between services
  Data consistency via distributed transaction patterns

🚀 API Layer Architecture
  Gateway Routing Structure
    /user/**     → User Service (8081)
    /book/**     → Book Service (8082) 
    /transaction/** → Transaction Service (8083)
    /analytics/** → Analytics Service (8084)

Service Communication Pattern
  -Synchronous: REST APIs with Feign clients
  -Discovery: Dynamic service resolution via Eureka
💾 Data Architecture
  Database Design
    Database per Service: Each microservice owns its data
    H2 In-Memory: Development and testing databases
    Connection Pooling: HikariCP for connection management
  Data Access Pattern
    Spring Data JPA: Repository abstraction layer
    Entity-Repository: Domain-driven data access
    Transaction Boundaries: Service-level transaction management
    Data Consistency: Eventually consistent across services