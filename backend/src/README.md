# Groceries Delivery Management (Spring WebFlux + PostgreSQL)

This project is a reactive Groceries Delivery Management application using **Spring WebFlux**, **R2DBC with PostgreSQL**, and following **TDD practices**.

## Tech Stack
- Java 17
- Spring WebFlux
- PostgreSQL (R2DBC)
- JUnit 5 & Mockito
- Maven

## Setup

### Database Configuration
Update `application.yml` with your PostgreSQL credentials.

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/groceries_db
    username: your_username
    password: your_password
```

### Running the Application
```bash
./mvnw spring-boot:run
```

### Run Tests
```bash
./mvnw test
```

### API Endpoints

| Method | Endpoint        | Description               |
|-------|----------------|----------------------|
| GET   | /groceries     | List all groceries    |
| POST  | /groceries     | Create a grocery item |

## Postman Collection
A `postman_collection.json` is included in the project for easier testing.