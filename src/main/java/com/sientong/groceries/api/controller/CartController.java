package com.sientong.groceries.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sientong.groceries.api.request.AddToCartRequest;
import com.sientong.groceries.api.request.UpdateCartItemRequest;
import com.sientong.groceries.api.response.CartResponse;
import com.sientong.groceries.api.response.CartSummaryResponse;
import com.sientong.groceries.domain.cart.CartService;
import com.sientong.groceries.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    private Mono<String> getCurrentUserId() {
        log.debug("Getting current user ID");
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .cast(Authentication.class)
            .map(Authentication::getPrincipal)
            .cast(UserPrincipal.class)
            .map(UserPrincipal::getId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found")))
            .doOnError(ex -> log.error("Error getting current user", ex))
            .subscribeOn(Schedulers.boundedElastic());
    }

    @Operation(
        summary = "Get user's cart",
        description = "Retrieve the current user's shopping cart. Only accessible by customers."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved cart"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user does not have CUSTOMER role"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Mono<ResponseEntity<CartResponse>> getCart() {
        return getCurrentUserId()
            .flatMap(userId -> {
                log.debug("Getting cart for user: {}", userId);
                return cartService.getCart(userId)
                    .map(CartResponse::fromDomain)
                    .map(ResponseEntity::ok)
                    .doOnError(ex -> log.error("Error getting cart", ex));
            });
    }

    @Operation(
        summary = "Add item to cart",
        description = "Add a product to the user's shopping cart. Only accessible by customers."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request - validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user does not have CUSTOMER role"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Mono<ResponseEntity<CartResponse>> addToCart(
        @Parameter(description = "Add to cart request", required = true)
        @Valid @RequestBody AddToCartRequest request
    ) {
        return getCurrentUserId()
            .flatMap(userId -> {
                log.debug("Adding item to cart for user: {}", userId);
                return cartService.addToCart(userId, request.toDomain())
                    .map(CartResponse::fromDomain)
                    .map(ResponseEntity::ok)
                    .doOnError(ex -> log.error("Error adding item to cart", ex));
            });
    }

    @Operation(
        summary = "Update cart item",
        description = "Update the quantity of an item in the cart. Only accessible by customers."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request - validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user does not have CUSTOMER role"),
        @ApiResponse(responseCode = "404", description = "Cart item not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Mono<ResponseEntity<CartResponse>> updateCartItem(
        @Parameter(description = "Cart item ID", required = true)
        @PathVariable String itemId,
        @Parameter(description = "Update cart item request", required = true)
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return getCurrentUserId()
            .flatMap(userId -> {
                log.debug("Updating item {} in cart for user: {}", itemId, userId);
                return cartService.updateCartItem(userId, itemId, request.toDomain())
                    .map(CartResponse::fromDomain)
                    .map(ResponseEntity::ok)
                    .doOnError(ex -> log.error("Error updating cart item", ex));
            });
    }

    @Operation(
        summary = "Remove item from cart",
        description = "Remove an item from the cart. Only accessible by customers."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item removed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user does not have CUSTOMER role"),
        @ApiResponse(responseCode = "404", description = "Cart item not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Mono<ResponseEntity<CartResponse>> removeItem(
        @Parameter(description = "Cart item ID", required = true)
        @PathVariable String itemId
    ) {
        return getCurrentUserId()
            .flatMap(userId -> {
                log.debug("Removing item {} from cart for user: {}", itemId, userId);
                return cartService.removeItem(userId, itemId)
                    .map(CartResponse::fromDomain)
                    .map(ResponseEntity::ok)
                    .doOnError(ex -> log.error("Error removing item from cart", ex));
            });
    }

    @Operation(
        summary = "Clear cart",
        description = "Remove all items from the cart. Only accessible by customers."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user does not have CUSTOMER role"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Mono<ResponseEntity<CartResponse>> clearCart() {
        return getCurrentUserId()
            .flatMap(userId -> {
                log.debug("Clearing cart for user: {}", userId);
                return cartService.clearCart(userId)
                    .map(CartResponse::fromDomain)
                    .map(ResponseEntity::ok)
                    .doOnError(ex -> log.error("Error clearing cart", ex));
            });
    }

    @Operation(
        summary = "Get cart summary",
        description = "Get a summary of the cart including total items and price. Only accessible by customers."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved cart summary"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user does not have CUSTOMER role"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Mono<ResponseEntity<CartSummaryResponse>> getCartSummary() {
        return getCurrentUserId()
            .flatMap(userId -> {
                log.debug("Getting cart summary for user: {}", userId);
                return cartService.getCartSummary(userId)
                    .map(CartSummaryResponse::fromDomain)
                    .map(ResponseEntity::ok)
                    .doOnError(ex -> log.error("Error getting cart summary", ex));
            });
    }
}
