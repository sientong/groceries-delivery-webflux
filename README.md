# Groceries Delivery System Backend

A reactive Spring Boot application for managing groceries delivery, featuring real-time order tracking, inventory management, and secure payment processing. Built following Clean Architecture and Domain-Driven Design principles.

## Development Environment

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Maven 3.6 or higher

### Environment Setup

The application uses environment-specific configurations:

1. **Development Environment** (`application-dev.properties`)
   - PostgreSQL on port 5433
   - Redis on port 6380
   - Debug port 5005
   - Detailed logging enabled
   - Management endpoints exposed
   - Default JWT configuration

2. **Production Environment** (`application-prod.properties`)
   - Requires environment variables for sensitive data
   - SSL enabled by default
   - Limited management endpoints
   - Restricted logging
   - Secure JWT configuration

### Running with Docker (Development)

1. **Start the development environment**
   ```bash
   docker compose -f docker-compose.dev.yml up
   ```

   This will start:
   - Backend service (port 8080, debug port 5005)
   - PostgreSQL (port 5433)
   - Redis (port 6380)
   - Adminer (port 8081)
   - Redis Commander (port 8082)
   - Prometheus (port 9091)
   - Grafana (port 3001)

2. **Access development tools**
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI Spec: http://localhost:8080/v3/api-docs
   - Adminer (DB): http://localhost:8081
   - Redis Commander: http://localhost:8082
   - Grafana: http://localhost:3001 (admin/admin)

### Environment Variables

#### Development Defaults
```properties
SPRING_PROFILES_ACTIVE=dev
SPRING_R2DBC_URL=r2dbc:postgresql://postgres-dev:5432/groceries_dev
SPRING_R2DBC_USERNAME=postgres
SPRING_R2DBC_PASSWORD=postgres
SPRING_REDIS_HOST=redis-dev
SPRING_REDIS_PORT=6380
```

#### Production Requirements
```properties
SPRING_PROFILES_ACTIVE=prod
SPRING_R2DBC_URL=<required>
SPRING_R2DBC_USERNAME=<required>
SPRING_R2DBC_PASSWORD=<required>
SPRING_REDIS_HOST=<required>
SPRING_REDIS_PORT=<required>
JWT_SECRET=<required>
SERVER_SSL_KEYSTORE=<required>
SERVER_SSL_KEYSTORE_PASSWORD=<required>
```

## Architecture

### Clean Architecture Layers

- **API Layer** (`api/`)
  - Controllers: REST endpoints with OpenAPI documentation
  - Request/Response DTOs: Data transfer objects with validation
  - Security: JWT authentication and role-based access
  - Error Handling: Global exception handling and error responses

- **Domain Layer** (`domain/`)
  - Product Domain: Product catalog and inventory management
  - Order Domain: Order processing and tracking
  - User Domain: User management and authentication
  - Cart Domain: Shopping cart management
  - Services: Business logic implementation
  - Value Objects: Domain-specific types
  - Domain Events: Cross-boundary communication
  - Domain Exceptions: Business rule violations

- **Infrastructure Layer** (`infrastructure/`)
  - Persistence: Reactive repositories and database entities
  - Adapters: Implementation of domain interfaces
  - External Services: Third-party integrations
  - Error Translation: Technical to domain error mapping

## Security

### Authentication & Authorization

- **JWT-based Authentication**
  - Bearer token authentication
  - Token refresh mechanism
  - Secure token storage
  - Reactive security context handling

- **Role-Based Access Control (RBAC)**
  - Customer Role:
    - Manage personal profile
    - View and manage shopping cart
    - Place and view orders
  - Admin Role:
    - User management
    - Product management
    - Order management
    - Access monitoring endpoints

- **Method Security**
  - `@PreAuthorize` annotations for fine-grained control
  - Role-based endpoint restrictions
  - Custom security expressions
  - Reactive method security enabled

### API Security

- **Rate Limiting**
  - Per-user rate limits
  - IP-based rate limits
  - Custom rate limit policies

- **Secure Headers**
  - CORS configuration
  - CSP (Content Security Policy)
  - XSS protection
  - CSRF protection

- **Endpoint Security**
  - Public endpoints:
    - `/api/v1/auth/**` - Authentication
    - `/api/v1/products/**` - Product catalog
    - `/api/v1/categories/**` - Product categories
  - Protected endpoints:
    - `/api/v1/users/**` - User management (Admin)
    - `/api/v1/cart/**` - Shopping cart (Customer)
    - `/api/v1/orders/**` - Order management (Customer)
  - Admin-only endpoints:
    - `/actuator/**` - Monitoring endpoints
    - `/api/v1/admin/**` - Admin operations

### Development Security

- **Testing**
  - Security integration tests
  - Role-based access tests
  - Authentication flow tests
  - Test security configuration

- **Configuration**
  - Environment-specific security settings
  - Secure password storage
  - Encrypted properties

## Features

### Cart Management
- Add/remove items
- Update quantities
- View cart summary
- Clear cart
- Role-based access (Customer only)

### Order Management
- Place orders
- Track order status
- View order history
- Update order status (Admin)
- Real-time notifications

### User Management
- User registration
- Profile management
- Password updates
- Role management (Admin)

## API Documentation

### Swagger UI

The API documentation is available through Swagger UI at `/swagger-ui.html`. Features:
- Interactive API exploration
- Request/response examples
- Authentication:
  1. Use `/api/v1/auth/login` to get JWT token
  2. Click "Authorize" button (🔓)
  3. Enter token: `Bearer <your_jwt_token>`
  4. All subsequent requests include the token
- Role-based access documentation
- Error response documentation

### OpenAPI Specification

Available at `/v3/api-docs`, includes:
- Detailed API endpoints
- Security schemes
- Data models
- Error responses
- Rate limiting info

### Error Responses

Standard error responses:
- 400: Bad Request - Invalid input
- 401: Unauthorized - Missing/invalid token
- 403: Forbidden - Insufficient permissions
- 404: Not Found - Resource doesn't exist
- 429: Too Many Requests - Rate limit exceeded
- 500: Internal Server Error

## Technology Stack

- **Backend Framework**: Spring Boot 3.2.3 + WebFlux
- **Database**: PostgreSQL with R2DBC
- **Cache**: Redis
- **Security**: Spring Security with JWT
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven
- **Testing**: JUnit 5, WebTestClient
- **Monitoring**: Prometheus + Grafana

## Local Development

### Running without Docker

1. **Configure local PostgreSQL**
   ```properties
   spring.r2dbc.url=r2dbc:postgresql://localhost:5432/groceries_db
   spring.r2dbc.username=postgres
   spring.r2dbc.password=your_password
   ```

2. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Test Accounts

For development and testing:

1. **Admin Account**
   - Email: admin@groceries.com
   - Password: password123
   - Role: ADMIN

2. **Customer Accounts**
   - John Doe (john@example.com)
   - Jane Smith (jane@example.com)
   - Password: password123
   - Role: CUSTOMER

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for new features
4. Ensure all tests pass
5. Create a Pull Request

## License

MIT License
