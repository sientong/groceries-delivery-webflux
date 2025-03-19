package com.sientong.groceries.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sientong.groceries.domain.user.User;
import com.sientong.groceries.domain.user.UserRole;
import com.sientong.groceries.domain.user.UserService;
import com.sientong.groceries.security.IJwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    private final UserService userService;
    private final IJwtService jwtService;
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
    public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (request.getEmail() == null || request.getPassword() == null || 
            request.getFirstName() == null || request.getLastName() == null || 
            request.getRole() == null) {
            return Mono.just(ResponseEntity.badRequest().<AuthResponse>build());
        }

        return userService.findByEmail(request.getEmail())
                .map(existingUser -> ResponseEntity.status(HttpStatus.CONFLICT).<AuthResponse>build())
                .switchIfEmpty(
                    Mono.fromCallable(() -> {
                        User user = request.toDomain();
                        user.setPassword(passwordEncoder.encode(request.getPassword()));
                        user.setEnabled(true);
                        user.setAccountNonExpired(true);
                        user.setAccountNonLocked(true);
                        user.setCredentialsNonExpired(true);
                        return user;
                    })
                    .flatMap(userService::createUser)
                    .map(user -> {
                        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
                        return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new AuthResponse(token, user));
                    })
                );
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
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return userService.findByEmail(request.getEmail())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).<AuthResponse>build());
                    }
                    String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
                    return Mono.just(ResponseEntity.ok(new AuthResponse(token, user)));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).<AuthResponse>build()));
    }

    @Operation(
        summary = "Validate JWT token",
        description = "Validate the provided JWT token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token is valid",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Invalid token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/validate")
    public Mono<ResponseEntity<AuthResponse>> validateToken(@Parameter(description = "JWT token", required = true)
        @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        String token = authHeader.substring(7);
        String username = jwtService.validateTokenAndGetUsername(token);
        if (username == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return userService.findByEmail(username)
                .map(user -> ResponseEntity.ok(new AuthResponse(token, user)))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    @Operation(
        summary = "Refresh JWT token",
        description = "Refresh the provided JWT token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Invalid token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@Parameter(description = "JWT token", required = true)
        @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        String token = authHeader.substring(7);
        String username = jwtService.validateTokenAndGetUsername(token);
        if (username == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return userService.findByEmail(username)
                .map(user -> {
                    String newToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
                    return ResponseEntity.ok(new AuthResponse(newToken, user));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    @Operation(
        summary = "Logout",
        description = "Logout the user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout() {
        // Since we're using JWT, we don't need to do anything server-side
        // The client should remove the token from storage
        return Mono.just(ResponseEntity.ok().build());
    }

    @Schema(description = "User registration request")
    @Data
    public static class RegisterRequest {
        @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @Schema(description = "User password", example = "password123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @Schema(description = "User first name", example = "John")
        @NotBlank(message = "First name is required")
        private String firstName;

        @Schema(description = "User last name", example = "Doe")
        @NotBlank(message = "Last name is required")
        private String lastName;

        @Schema(description = "User role", example = "USER")
        @NotNull(message = "Role is required")
        private UserRole role;

        public User toDomain() {
            return User.builder()
                    .email(email)
                    .password(password)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .build();
        }
    }

    @Schema(description = "User login request")
    @Data
    public static class LoginRequest {
        @Schema(description = "User email", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @Schema(description = "User password", example = "password123")
        @NotBlank(message = "Password is required")
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
