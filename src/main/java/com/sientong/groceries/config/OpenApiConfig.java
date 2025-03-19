package com.sientong.groceries.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Groceries Delivery System API",
        version = "1.0",
        description = """
            RESTful API for the Groceries Delivery System
            
            ## Authentication
            The API uses JWT Bearer token authentication. Include the token in the Authorization header:
            ```
            Authorization: Bearer <token>
            ```
            
            ## Roles and Permissions
            - **Customer**: Can manage their profile, cart, and orders
            - **Admin**: Can manage users, products, and access monitoring endpoints
            
            ## Rate Limiting
            API endpoints are rate-limited based on user role and IP address.
            
            ## Error Responses
            - 400: Bad Request - Invalid input
            - 401: Unauthorized - Missing or invalid token
            - 403: Forbidden - Insufficient permissions
            - 404: Not Found - Resource doesn't exist
            - 429: Too Many Requests - Rate limit exceeded
            - 500: Internal Server Error
            """,
        contact = @Contact(
            name = "Development Team",
            email = "dev@groceries.com",
            url = "https://groceries.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Development server"
        ),
        @Server(
            url = "https://api.groceries.com",
            description = "Production server"
        )
    }
)
@SecuritySchemes({
    @SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = """
            JWT Bearer token authentication. Obtain a token from the /api/v1/auth/login endpoint.
            Include the token in the Authorization header:
            ```
            Authorization: Bearer <token>
            ```
            """
    ),
    @SecurityScheme(
        name = "API Key",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-KEY",
        description = "API key for monitoring endpoints (Admin only)"
    )
})
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components())
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .tags(Arrays.asList(
                new Tag().name("Auth").description("Authentication endpoints"),
                new Tag().name("Users").description("User management endpoints"),
                new Tag().name("Cart").description("Shopping cart management endpoints"),
                new Tag().name("Orders").description("Order management endpoints"),
                new Tag().name("Products").description("Product catalog endpoints"),
                new Tag().name("Categories").description("Product category endpoints"),
                new Tag().name("Admin").description("Administrative endpoints"),
                new Tag().name("Monitoring").description("System monitoring endpoints")
            ));
    }
}
