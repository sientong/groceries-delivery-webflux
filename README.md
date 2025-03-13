# Groceries Delivery System

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
   - Adminer (DB management): http://localhost:8081
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
  - Notification Domain: Real-time notifications
  - Services: Business logic implementation
  - Value Objects: Domain-specific types
  - Domain Events: Cross-boundary communication
  - Domain Exceptions: Business rule violations

- **Infrastructure Layer** (`infrastructure/`)
  - Persistence: Reactive repositories and database entities
  - Adapters: Implementation of domain interfaces
  - External Services: Third-party integrations
  - Error Translation: Technical to domain error mapping

## Features

- **Product Management**
  - CRUD operations for products
  - Category-based product organization
  - Real-time stock management with Quantity value objects
  - Role-based access control (SELLER permissions)

- **Order Management**
  - Place and track orders
  - Real-time order status updates
  - Secure checkout process
  - Order history
  - Delivery information management
  - Domain events for cross-boundary communication

- **User Management**
  - JWT-based authentication
  - Role-based authorization (USER, SELLER, ADMIN)
  - User profile management
  - Secure password handling with BCrypt

- **Real-time Notifications**
  - Server-Sent Events (SSE) for real-time updates
  - Order status notifications
  - Stock alerts for sellers
  - Unread notifications tracking

## Technology Stack

- **Backend Framework**: Spring Boot 3.2.3 + WebFlux
- **Database**: PostgreSQL with R2DBC for reactive data access
- **Migration**: Flyway
- **Security**: Spring Security with JWT
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven

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

3. **Driver Account**
   - Mike Driver (driver1@groceries.com)
   - Password: password123
   - Role: DRIVER

## API Documentation

### Accessing API Documentation

The API documentation is available through Swagger UI and OpenAPI:

1. **Swagger UI**
   - URL: http://localhost:8080/swagger-ui.html
   - Interactive documentation with try-it-out functionality
   - Authentication:
     1. Use the `/api/v1/auth/login` endpoint to get a JWT token
     2. Click the "Authorize" button (🔓) at the top
     3. Enter the token in format: `Bearer <your_jwt_token>`
     4. All subsequent requests will include the token

2. **OpenAPI JSON**
   - URL: http://localhost:8080/api-docs
   - Raw OpenAPI specification
   - Useful for generating client code

3. **Swagger Features**
   - Interactive API testing
   - Request/response examples
   - Schema validation
   - Error responses
   - Security requirements
   - Model definitions

### Testing with Swagger UI

1. **Authentication**
   ```json
   POST /api/v1/auth/login
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```
   Copy the JWT token from the response.

2. **Authorization**
   - Click the "Authorize" button (🔓)
   - Enter token: `Bearer eyJhbGciOiJ...` (your JWT token)
   - All secured endpoints will now work

3. **Testing Flow Example**
   1. Create a new order:
      ```json
      POST /api/v1/orders
      {
        "items": [
          {
            "productId": "123",
            "quantity": 2
          }
        ]
      }
      ```

   2. Track the order:
      ```
      GET /api/v1/orders/{orderId}/track
      ```

   3. Update delivery info (SELLER only):
      ```json
      PATCH /api/v1/orders/{orderId}/delivery-info
      {
        "address": "123 Main St",
        "phone": "+1234567890",
        "trackingNumber": "TRK123",
        "estimatedDeliveryTime": "2025-03-13T14:30:00",
        "deliveryNotes": "Leave at door"
      }
      ```

4. **Error Testing**
   - Try requests without authentication
   - Submit invalid data formats
   - Test business rule violations
   - Check error response formats

### API Versioning

The API uses URI versioning with the format `/api/v{n}/...` where n is the version number.
Current version: v1

Benefits of this approach:
- Clear and explicit versioning
- Easy to route and document
- Compatible with API gateways and proxies

### Error Handling

All API errors follow a consistent format:

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "timestamp": "2025-03-12T11:46:19+07:00",
  "details": [
    "Address cannot be blank",
    "Invalid phone number format"
  ]
}
```

Common HTTP status codes:
- 200: Success
- 201: Created
- 400: Bad Request (validation errors)
- 401: Unauthorized (missing/invalid token)
- 403: Forbidden (insufficient permissions)
- 404: Not Found
- 409: Conflict (e.g., duplicate entry)
- 500: Internal Server Error

### Available Endpoints

#### Authentication
- `POST /api/v1/auth/register`: Register new user
  - Request: User registration details
  - Response: JWT token and user details
  - Errors: 400 (validation), 409 (duplicate email)

- `POST /api/v1/auth/login`: User login
  - Request: Email and password
  - Response: JWT token and user details
  - Errors: 401 (invalid credentials)

#### Products (Public)
- `GET /api/v1/products`: List all products
- `GET /api/v1/products/{id}`: Get product by ID
- `GET /api/v1/products/category/{category}`: Get products by category

#### Products (SELLER only)
- `POST /api/v1/products`: Create new product
- `PUT /api/v1/products/{id}`: Update product
- `DELETE /api/v1/products/{id}`: Delete product
- `PATCH /api/v1/products/{id}/stock`: Update product stock
  - Request: Quantity delta (increase/decrease)
  - Errors: 400 (invalid quantity), 404 (product not found)

#### Orders (Authenticated Users)
- `POST /api/v1/orders`: Place new order
  - Request: List of order items
  - Response: Created order with status
  - Errors: 400 (validation), 409 (insufficient stock)

- `GET /api/v1/orders/{id}/track`: Track order status
  - Response: Order with delivery information
  - Errors: 404 (order not found)

- `POST /api/v1/orders/{id}/checkout`: Process order checkout
- `POST /api/v1/orders/{id}/cancel`: Cancel order
  - Errors: 400 (invalid state), 404 (order not found)

- `PATCH /api/v1/orders/{id}/delivery-info`: Update delivery information (SELLER only)
  - Request: Address, phone, tracking number, estimated delivery time
  - Response: Updated order with delivery status
  - Errors: 400 (validation), 404 (order not found)

#### Notifications (Authenticated Users)
- `GET /api/v1/notifications/stream`: SSE stream for real-time notifications
- `GET /api/v1/notifications`: Get user notifications
- `GET /api/v1/notifications/unread/count`: Get unread notifications count
- `PUT /api/v1/notifications/{id}/read`: Mark notification as read

#### Users (ADMIN only)
- `GET /api/v1/users`: List all users
- `GET /api/v1/users/{id}`: Get user by ID
- `PUT /api/v1/users/{id}`: Update user
- `DELETE /api/v1/users/{id}`: Delete user

### API Security

- Public endpoints:
  - `GET /api/v1/products/**`: Product browsing
  - `/api/v1/auth/**`: Authentication endpoints

- Protected endpoints:
  - `POST/PUT/DELETE /api/v1/products/**`: SELLER role required
  - `/api/v1/orders/**`: Authenticated users
  - `/api/v1/notifications/**`: Authenticated users
  - `/api/v1/users/**`: ADMIN role required

### Request/Response DTOs

All DTOs include comprehensive OpenAPI documentation with:
- Field descriptions and examples
- Validation rules
- Required field markers
- Response schemas
- Error scenarios

## Monitoring & Metrics

### Actuator Endpoints

The application exposes Spring Boot Actuator endpoints for monitoring:

- Health check: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`
- Info: `/actuator/info`

Secured with basic authentication:
```
Username: actuator
Password: actuator123
```

### Prometheus Metrics

Key JVM and application metrics:

1. Memory Metrics:
```promql
# Heap Memory Usage
jvm_memory_used_bytes{area="heap"}

# Memory Usage Percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100
```

2. HTTP Metrics:
```promql
# Request Rate
rate(http_server_requests_seconds_count[5m])

# Average Response Time
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Error Rate
(sum(rate(http_server_requests_seconds_count{status="5xx"}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))) * 100
```

3. Database Metrics:
```promql
# Active Connections
r2dbc_pool_acquired_connections

# Connection Acquisition Time
r2dbc_pool_pending_time_seconds
```

4. Business Metrics:
```promql
# Active Orders
order_active_total

# Order Processing Time
order_processing_time_seconds
```

### Grafana Dashboards

Pre-configured dashboards available in `/grafana/dashboards/`:

1. Application Overview:
   - System health status
   - Key performance indicators
   - Error rates and latencies

2. JVM Metrics:
   - Memory usage
   - Garbage collection
   - Thread states
   - CPU usage

3. HTTP Metrics:
   - Request rates
   - Response times
   - Status codes
   - Endpoint performance

4. Business Metrics:
   - Order processing
   - User activities
   - Inventory status

### Development Tools

1. Remote Debugging:
   - Port: 5005
   - Already configured in Dockerfile.dev
   - Connect using your IDE's remote debugger

2. Hot Reload:
   - Automatic reloading of changed classes
   - Preserves application state
   - Speeds up development cycle

## Domain Model

The application follows DDD principles with the following aggregates:
- **Product**: Product catalog and inventory management
- **Order**: Order processing and delivery tracking
- **User**: User authentication and profile management
- **Notification**: Real-time event notifications

Each aggregate has its own repository interface in the domain layer, with implementations in the infrastructure layer.

## Database Schema

The application uses a PostgreSQL database with the following main tables:
- `users`: User authentication and profile data
- `products`: Product catalog and inventory
- `orders`: Order management
- `order_items`: Order line items
- `notifications`: User notifications

Database migrations are managed by Flyway and can be found in `src/main/resources/db/migration`.

## Testing

Run the tests using:
```bash
mvn test
```

The test suite includes:
- Unit tests for domain services
- Integration tests for repositories
- API tests for controllers
- Security tests for authentication
- Error handling tests
- Validation tests

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
