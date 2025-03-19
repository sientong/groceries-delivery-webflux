package com.sientong.groceries.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.sientong.groceries.api.controller.CartController;
import com.sientong.groceries.api.request.CartItemRequest;
import com.sientong.groceries.config.TestSecurityConfig;
import com.sientong.groceries.domain.cart.Cart;
import com.sientong.groceries.domain.cart.CartItem;
import com.sientong.groceries.domain.cart.CartService;
import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.user.UserRole;
import com.sientong.groceries.security.UserPrincipal;

import reactor.core.publisher.Mono;

/**
 * Integration tests for CartController.
 * Tests cart management endpoints with proper security context handling.
 * Security is enforced through Spring Security and TestSecurityConfig.
 * 
 * Test cases cover:
 * 1. Cart retrieval and management operations
 * 2. Authentication requirements
 * 3. Empty cart handling (valid state, not error)
 * 4. Security context handling
 * 5. Role-based access control
 */
@WebFluxTest(CartController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureWebTestClient(timeout = "100000")
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    private Cart testCart;
    private CartItem testItem;
    private static final String TEST_USER_ID = "test-user";
    private static final String TEST_CART_ID = "test-cart";
    private static final String TEST_ITEM_ID = "test-item";

    @BeforeEach
    void setUp() {
        testItem = CartItem.builder()
                .id(TEST_ITEM_ID)
                .productId("test-product")
                .name("Test Product")
                .description("Test Description")
                .price(Money.of(BigDecimal.TEN, "USD"))
                .quantity(1)
                .unit("pcs")
                .build();

        testCart = Cart.builder()
                .id(TEST_CART_ID)
                .userId(TEST_USER_ID)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();
        testCart.addItem(testItem);

        // Set up default response for getCart - always return a cart, never empty
        when(cartService.getCart(anyString()))
                .thenReturn(Mono.just(testCart));
    }

    private Authentication createCustomerAuthentication() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(TEST_USER_ID)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .password("Password123@")
                .build();
        return new TestingAuthenticationToken(principal, principal.getPassword(), 
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRole().name())));
    }

    private Authentication createAdminAuthentication() {
        UserPrincipal principal = UserPrincipal.builder()
                .id("admin-user")
                .firstName("Admin")
                .lastName("User")
                .email("admin@example.com")
                .role(UserRole.ADMIN)
                .enabled(true)
                .password("Password123@")
                .build();
        return new TestingAuthenticationToken(principal, principal.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRole().name())));
    }

    @Test
    void shouldAllowGetCartWithCustomerRole() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createCustomerAuthentication()))
                .get()
                .uri("/api/v1/cart")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(TEST_CART_ID)
                .jsonPath("$.userId").isEqualTo(TEST_USER_ID)
                .jsonPath("$.items[0].id").isEqualTo(TEST_ITEM_ID);
    }

    @Test
    void shouldDenyGetCartWithAdminRole() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createAdminAuthentication()))
                .get()
                .uri("/api/v1/cart")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldAllowAddToCartWithCustomerRole() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId("test-product");
        request.setQuantity(1);
        request.setUnit("pcs");

        when(cartService.addToCart(anyString(), any(CartItem.class)))
                .thenReturn(Mono.just(testCart));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createCustomerAuthentication()))
                .post()
                .uri("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].id").isEqualTo(TEST_ITEM_ID);
    }

    @Test
    void shouldDenyAddToCartWithAdminRole() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId("test-product");
        request.setQuantity(1);
        request.setUnit("pcs");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createAdminAuthentication()))
                .post()
                .uri("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldAllowUpdateCartItemWithCustomerRole() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId("test-product");
        request.setQuantity(2);
        request.setUnit("pcs");

        when(cartService.updateCartItem(anyString(), anyString(), any(CartItem.class)))
                .thenReturn(Mono.just(testCart));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createCustomerAuthentication()))
                .put()
                .uri("/api/v1/cart/items/" + TEST_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].id").isEqualTo(TEST_ITEM_ID);
    }

    @Test
    void shouldDenyUpdateCartItemWithAdminRole() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId("test-product");
        request.setQuantity(2);
        request.setUnit("pcs");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createAdminAuthentication()))
                .put()
                .uri("/api/v1/cart/items/" + TEST_ITEM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldAllowRemoveItemWithCustomerRole() {
        Cart emptyCart = Cart.builder()
                .id(TEST_CART_ID)
                .userId(TEST_USER_ID)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.removeItem(anyString(), anyString()))
                .thenReturn(Mono.just(emptyCart));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createCustomerAuthentication()))
                .delete()
                .uri("/api/v1/cart/items/" + TEST_ITEM_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isEmpty();
    }

    @Test
    void shouldDenyRemoveItemWithAdminRole() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createAdminAuthentication()))
                .delete()
                .uri("/api/v1/cart/items/" + TEST_ITEM_ID)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldAllowClearCartWithCustomerRole() {
        Cart emptyCart = Cart.builder()
                .id(TEST_CART_ID)
                .userId(TEST_USER_ID)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.clearCart(anyString()))
                .thenReturn(Mono.just(emptyCart));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createCustomerAuthentication()))
                .delete()
                .uri("/api/v1/cart")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items").isEmpty();
    }

    @Test
    void shouldDenyClearCartWithAdminRole() {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createAdminAuthentication()))
                .delete()
                .uri("/api/v1/cart")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldRequireAuthentication() {
        // Test GET /api/v1/cart
        webTestClient.get()
                .uri("/api/v1/cart")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();

        // Test POST /api/v1/cart/items
        CartItemRequest request = new CartItemRequest();
        request.setProductId("test-product");
        request.setQuantity(1);
        request.setUnit("pcs");

        webTestClient.post()
                .uri("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldHandleEmptyCart() {
        // Given: An empty cart (valid state, not an error)
        Cart emptyCart = Cart.builder()
                .id(TEST_CART_ID)
                .userId(TEST_USER_ID)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartService.getCart(anyString()))
                .thenReturn(Mono.just(emptyCart));  // Return empty cart, never Mono.empty()

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(createCustomerAuthentication()))
                .get()
                .uri("/api/v1/cart")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Empty cart is a valid state
                .expectBody()
                .jsonPath("$.items").isEmpty()
                .jsonPath("$.total").isEqualTo("0");
    }
}
