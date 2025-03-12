package com.sientong.groceries.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sientong.groceries.domain.user.User;
import com.sientong.groceries.domain.user.UserService;
import com.sientong.groceries.security.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Operation(
        summary = "Register new user",
        description = "Register a new user with the provided details"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthResponse> register(
        @Parameter(description = "Registration details", required = true)
        @Valid @RequestBody RegisterRequest request
    ) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .build();

        return userService.createUser(user)
                .map(createdUser -> {
                    String token = jwtService.generateToken(
                        createdUser.getEmail(),
                        createdUser.getRole().name()
                    );
                    return new AuthResponse(token, createdUser);
                });
    }

    @Operation(
        summary = "User login",
        description = "Authenticate user and generate JWT token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public Mono<AuthResponse> login(
        @Parameter(description = "Login credentials", required = true)
        @Valid @RequestBody LoginRequest request
    ) {
        return userService.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtService.generateToken(
                        user.getEmail(),
                        user.getRole().name()
                    );
                    return new AuthResponse(token, user);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid credentials")));
    }

    @Schema(description = "User registration request")
    @Data
    public static class RegisterRequest {
        @Schema(description = "User email", example = "user@example.com")
        private String email;
        
        @Schema(description = "User password", example = "password123")
        private String password;
        
        @Schema(description = "User first name", example = "John")
        private String firstName;
        
        @Schema(description = "User last name", example = "Doe")
        private String lastName;
        
        @Schema(description = "User role", example = "USER")
        private com.sientong.groceries.domain.user.UserRole role;
    }

    @Schema(description = "User login request")
    @Data
    public static class LoginRequest {
        @Schema(description = "User email", example = "user@example.com")
        private String email;
        
        @Schema(description = "User password", example = "password123")
        private String password;
    }

    @Schema(description = "Authentication response")
    @Data
    public static class AuthResponse {
        @Schema(description = "JWT token for authentication")
        private final String token;
        
        @Schema(description = "Authenticated user details")
        private final User user;
    }
}
