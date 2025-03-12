# Groceries Delivery System

A reactive Spring Boot application for managing groceries delivery, featuring real-time order tracking, inventory management, and secure payment processing. Built following Clean Architecture and Domain-Driven Design principles.

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

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/groceries-delivery-system.git
   cd groceries-delivery-system/backend
   ```

2. **Configure the database**
   
   Update `application.yml` with your PostgreSQL credentials:
   ```yaml
   spring:
     r2dbc:
       url: r2dbc:postgresql://localhost:5432/groceries_db
       username: your_username
       password: your_password
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`

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
