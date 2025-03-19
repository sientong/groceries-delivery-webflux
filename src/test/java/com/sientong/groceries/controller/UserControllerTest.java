package com.sientong.groceries.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.sientong.groceries.api.controller.UserController;
import com.sientong.groceries.api.request.UpdatePasswordRequest;
import com.sientong.groceries.api.request.UpdateProfileRequest;
import com.sientong.groceries.config.TestSecurityConfig;
import com.sientong.groceries.domain.user.User;
import com.sientong.groceries.domain.user.UserRole;
import com.sientong.groceries.domain.user.UserService;
import com.sientong.groceries.security.UserPrincipal;

import reactor.core.publisher.Mono;

/**
 * Integration tests for UserController.
 * Tests user profile management endpoints with proper security context handling.
 * Security is enforced through Spring Security and TestSecurityConfig.
 */
@WebFluxTest(UserController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureWebTestClient(timeout = "100000")
class UserControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123@";
    private static final String ADMIN_USER_ID = "admin-id";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    private User testUser; 
    private User adminUser;
    private UserPrincipal testUserPrincipal;
    private UserPrincipal adminUserPrincipal;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .firstName("Test")
                .lastName("User")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .password(TEST_PASSWORD)
                .build();

        adminUser = User.builder()
                .id(ADMIN_USER_ID)
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(UserRole.ADMIN)
                .enabled(true)
                .password(TEST_PASSWORD)
                .build();

        testUserPrincipal = UserPrincipal.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .firstName("Test")
                .lastName("User")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .password(TEST_PASSWORD)
                .build();

        adminUserPrincipal = UserPrincipal.builder()
                .id(ADMIN_USER_ID)
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(UserRole.ADMIN)
                .enabled(true)
                .password(TEST_PASSWORD)
                .build();

        // Set up default successful response for findById
        when(userService.findById(eq(TEST_USER_ID)))
                .thenReturn(Mono.just(testUser));
        when(userService.findById(eq(ADMIN_USER_ID)))
                .thenReturn(Mono.just(adminUser));
    }

    @Test
    void shouldAllowGetCurrentUserWithCustomerRole() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                    new TestingAuthenticationToken(testUserPrincipal, TEST_PASSWORD, "ROLE_" + UserRole.CUSTOMER.name())))
                .get()
                .uri("/api/v1/users/me")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(TEST_USER_ID)
                .jsonPath("$.email").isEqualTo(TEST_EMAIL)
                .jsonPath("$.firstName").isEqualTo("Test")
                .jsonPath("$.lastName").isEqualTo("User")
                .jsonPath("$.role").isEqualTo("CUSTOMER")
                .jsonPath("$.enabled").isEqualTo(true);
    }

    @Test
    void shouldAllowGetCurrentUserWithAdminRole() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                    new TestingAuthenticationToken(adminUserPrincipal, TEST_PASSWORD, "ROLE_" + UserRole.ADMIN.name())))
                .get()
                .uri("/api/v1/users/me")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(ADMIN_USER_ID)
                .jsonPath("$.email").isEqualTo("admin@example.com")
                .jsonPath("$.firstName").isEqualTo("Admin")
                .jsonPath("$.lastName").isEqualTo("User")
                .jsonPath("$.role").isEqualTo("ADMIN")
                .jsonPath("$.enabled").isEqualTo(true);
    }

    @Test
    void shouldAllowUpdateProfileWithCustomerRole() {
        var request = UpdateProfileRequest.builder()
                .firstName("Updated")
                .lastName("User")
                .email("updated@example.com")
                .build();

        var updatedUser = User.builder()
                .id(TEST_USER_ID)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();

        when(userService.updateProfile(eq(TEST_USER_ID), eq(request)))
                .thenReturn(Mono.just(updatedUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                    new TestingAuthenticationToken(testUserPrincipal, TEST_PASSWORD, "ROLE_" + UserRole.CUSTOMER.name())))
                .put()
                .uri("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo(request.getFirstName())
                .jsonPath("$.lastName").isEqualTo(request.getLastName())
                .jsonPath("$.email").isEqualTo(request.getEmail());
    }

    @Test
    void shouldAllowUpdatePasswordWithCustomerRole() {
        var request = UpdatePasswordRequest.builder()
                .currentPassword(TEST_PASSWORD)
                .newPassword("NewPassword123@")
                .confirmPassword("NewPassword123@")
                .build();

        when(userService.updatePassword(eq(TEST_USER_ID), eq(request)))
                .thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                    new TestingAuthenticationToken(testUserPrincipal, TEST_PASSWORD, "ROLE_" + UserRole.CUSTOMER.name())))
                .put()
                .uri("/api/v1/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldHandleInvalidPasswordUpdate() {
        var request = UpdatePasswordRequest.builder()
                .currentPassword("")
                .newPassword("")
                .confirmPassword("")
                .build();

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                    new TestingAuthenticationToken(testUserPrincipal, TEST_PASSWORD, "ROLE_" + UserRole.CUSTOMER.name())))
                .put()
                .uri("/api/v1/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldHandleInvalidProfileUpdate() {
        var request = UpdateProfileRequest.builder()
                .firstName("")
                .lastName("")
                .email("")
                .build();

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                    new TestingAuthenticationToken(testUserPrincipal, TEST_PASSWORD, "ROLE_" + UserRole.CUSTOMER.name())))
                .put()
                .uri("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldRequireAuthentication() {
        webTestClient
                .get()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient
                .put()
                .uri("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UpdateProfileRequest.builder()
                    .firstName("Test")
                    .lastName("User")
                    .email("test@example.com")
                    .build())
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient
                .put()
                .uri("/api/v1/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UpdatePasswordRequest.builder()
                    .currentPassword("old")
                    .newPassword("new")
                    .confirmPassword("new")
                    .build())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldHandleMissingUser() {
        when(userService.findById(eq(TEST_USER_ID)))
                .thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                    new TestingAuthenticationToken(testUserPrincipal, TEST_PASSWORD, "ROLE_" + UserRole.CUSTOMER.name())))
                .get()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldHandleServiceError() {
        when(userService.findById(eq(TEST_USER_ID)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                    new TestingAuthenticationToken(testUserPrincipal, TEST_PASSWORD, "ROLE_" + UserRole.CUSTOMER.name())))
                .get()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldHandleMissingSecurityContext() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser().roles())
                .get()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
