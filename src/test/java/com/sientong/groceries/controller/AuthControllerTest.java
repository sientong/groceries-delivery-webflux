package com.sientong.groceries.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.sientong.groceries.api.controller.AuthController;
import com.sientong.groceries.api.controller.AuthController.LoginRequest;
import com.sientong.groceries.api.controller.AuthController.RegisterRequest;
import com.sientong.groceries.config.TestSecurityConfig;
import com.sientong.groceries.domain.user.User;
import com.sientong.groceries.domain.user.UserRole;
import com.sientong.groceries.domain.user.UserService;
import com.sientong.groceries.security.IJwtService;

import reactor.core.publisher.Mono;

@WebFluxTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private UserService userService;

    @MockBean
    private IJwtService jwtService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String testToken;
    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user1")
                .email("test@example.com")
                .password("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        testToken = "test.jwt.token";

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setFirstName("John");
        validRegisterRequest.setLastName("Doe");
        validRegisterRequest.setRole(UserRole.CUSTOMER);

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn(testToken);
    }

    @Test
    void shouldRegisterNewUser() {
        when(userService.findByEmail(validRegisterRequest.getEmail())).thenReturn(Mono.empty());
        when(userService.createUser(argThat(user -> 
            user.getEmail().equals(validRegisterRequest.getEmail()) &&
            user.getPassword().equals("hashedPassword") &&
            user.getFirstName().equals(validRegisterRequest.getFirstName()) &&
            user.getLastName().equals(validRegisterRequest.getLastName()) &&
            user.getRole().equals(validRegisterRequest.getRole()) &&
            user.isEnabled() &&
            user.isAccountNonExpired() &&
            user.isAccountNonLocked() &&
            user.isCredentialsNonExpired()
        ))).thenReturn(Mono.just(testUser));

        webClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRegisterRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.token").isEqualTo(testToken)
                .jsonPath("$.user.email").isEqualTo(testUser.getEmail())
                .jsonPath("$.user.firstName").isEqualTo(testUser.getFirstName())
                .jsonPath("$.user.lastName").isEqualTo(testUser.getLastName())
                .jsonPath("$.user.role").isEqualTo(testUser.getRole().name());
    }

    @Test
    void shouldReturn400WhenRegisterRequestInvalid() {
        RegisterRequest invalidRequest = new RegisterRequest();
        // Missing all required fields

        webClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenEmailInvalid() {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");
        invalidRequest.setFirstName("John");
        invalidRequest.setLastName("Doe");
        invalidRequest.setRole(UserRole.CUSTOMER);

        webClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenPasswordTooShort() {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("123"); // Too short
        invalidRequest.setFirstName("John");
        invalidRequest.setLastName("Doe");
        invalidRequest.setRole(UserRole.CUSTOMER);

        webClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnConflictWhenEmailExists() {
        when(userService.findByEmail(validRegisterRequest.getEmail()))
            .thenReturn(Mono.just(testUser));

        webClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRegisterRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldLoginSuccessfully() {
        when(userService.findByEmail(validLoginRequest.getEmail())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validLoginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo(testToken)
                .jsonPath("$.user.email").isEqualTo(testUser.getEmail())
                .jsonPath("$.user.firstName").isEqualTo(testUser.getFirstName())
                .jsonPath("$.user.lastName").isEqualTo(testUser.getLastName())
                .jsonPath("$.user.role").isEqualTo(testUser.getRole().name());
    }

    @Test
    void shouldReturn400WhenLoginRequestInvalid() {
        LoginRequest invalidRequest = new LoginRequest();
        // Missing all required fields

        webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnUnauthorizedWhenEmailNotFound() {
        when(userService.findByEmail(validLoginRequest.getEmail())).thenReturn(Mono.empty());

        webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validLoginRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnUnauthorizedWhenPasswordIncorrect() {
        when(userService.findByEmail(validLoginRequest.getEmail())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validLoginRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        when(jwtService.validateTokenAndGetUsername(testToken)).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Mono.just(testUser));

        webClient.post()
                .uri("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo(testToken)
                .jsonPath("$.user.email").isEqualTo(testUser.getEmail())
                .jsonPath("$.user.firstName").isEqualTo(testUser.getFirstName())
                .jsonPath("$.user.lastName").isEqualTo(testUser.getLastName())
                .jsonPath("$.user.role").isEqualTo(testUser.getRole().name());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenInvalid() {
        when(jwtService.validateTokenAndGetUsername(testToken)).thenReturn(null);

        webClient.post()
                .uri("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenUserNotFound() {
        when(jwtService.validateTokenAndGetUsername(testToken)).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Mono.empty());

        webClient.post()
                .uri("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        String newToken = "new.jwt.token";
        when(jwtService.validateTokenAndGetUsername(testToken)).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Mono.just(testUser));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn(newToken);

        webClient.post()
                .uri("/api/v1/auth/refresh")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo(newToken)
                .jsonPath("$.user.email").isEqualTo(testUser.getEmail())
                .jsonPath("$.user.firstName").isEqualTo(testUser.getFirstName())
                .jsonPath("$.user.lastName").isEqualTo(testUser.getLastName())
                .jsonPath("$.user.role").isEqualTo(testUser.getRole().name());
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenInvalid() {
        when(jwtService.validateTokenAndGetUsername(testToken)).thenReturn(null);

        webClient.post()
                .uri("/api/v1/auth/refresh")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldLogoutSuccessfully() {
        webClient.post()
                .uri("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + testToken)
                .exchange()
                .expectStatus().isOk();
    }
}
