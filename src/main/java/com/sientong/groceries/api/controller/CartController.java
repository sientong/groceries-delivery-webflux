package com.sientong.groceries.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    @Operation(
        summary = "Get user's cart",
        description = "Retrieve the current user's shopping cart"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved cart"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Mono<ResponseEntity<CartResponse>> getCart() {
        return ReactiveSecurityContextHolder.getContext()
            .<Authentication>map(SecurityContext::getAuthentication)
            .<UserPrincipal>map(auth -> (UserPrincipal) auth.getPrincipal())
            .<String>map(UserPrincipal::getId)
            .flatMap(userId -> {
                log.debug("Getting cart for user: {}", userId);
                return cartService.getCart(userId)
                    .map(cart -> ResponseEntity.ok(CartResponse.fromDomain(cart)))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
            });
    }

    @Operation(
        summary = "Add item to cart",
        description = "Add a product to the user's shopping cart"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/items")
    public Mono<ResponseEntity<CartResponse>> addToCart(
        @Parameter(description = "Add to cart request", required = true)
        @Valid @RequestBody AddToCartRequest request
    ) {
        return ReactiveSecurityContextHolder.getContext()
            .<Authentication>map(SecurityContext::getAuthentication)
            .<UserPrincipal>map(auth -> (UserPrincipal) auth.getPrincipal())
            .<String>map(UserPrincipal::getId)
            .flatMap(userId -> {
                log.debug("Adding item to cart for user: {}", userId);
                return cartService.addToCart(userId, request.toDomain())
                    .map(cart -> ResponseEntity.ok(CartResponse.fromDomain(cart)));
            });
    }

    @Operation(
        summary = "Update cart item",
        description = "Update the quantity of an item in the cart"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "404", description = "Cart item not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/items/{itemId}")
    public Mono<ResponseEntity<CartResponse>> updateCartItem(
        @Parameter(description = "Cart item ID", required = true)
        @PathVariable String itemId,
        @Parameter(description = "Update cart item request", required = true)
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ReactiveSecurityContextHolder.getContext()
            .<Authentication>map(SecurityContext::getAuthentication)
            .<UserPrincipal>map(auth -> (UserPrincipal) auth.getPrincipal())
            .<String>map(UserPrincipal::getId)
            .flatMap(userId -> {
                log.debug("Updating cart item {} for user: {}", itemId, userId);
                return cartService.updateCartItem(userId, itemId, request.toDomain())
                    .map(cart -> ResponseEntity.ok(CartResponse.fromDomain(cart)))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
            });
    }

    @Operation(
        summary = "Remove cart item",
        description = "Remove an item from the cart"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item removed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "404", description = "Cart item not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/items/{itemId}")
    public Mono<ResponseEntity<CartResponse>> removeCartItem(
        @Parameter(description = "Cart item ID", required = true)
        @PathVariable String itemId
    ) {
        return ReactiveSecurityContextHolder.getContext()
            .<Authentication>map(SecurityContext::getAuthentication)
            .<UserPrincipal>map(auth -> (UserPrincipal) auth.getPrincipal())
            .<String>map(UserPrincipal::getId)
            .flatMap(userId -> {
                log.debug("Removing cart item {} for user: {}", itemId, userId);
                return cartService.removeCartItem(userId, itemId)
                    .map(cart -> ResponseEntity.ok(CartResponse.fromDomain(cart)))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
            });
    }

    @Operation(
        summary = "Clear cart",
        description = "Remove all items from the cart"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping
    public Mono<ResponseEntity<Void>> clearCart() {
        return ReactiveSecurityContextHolder.getContext()
            .<Authentication>map(SecurityContext::getAuthentication)
            .<UserPrincipal>map(auth -> (UserPrincipal) auth.getPrincipal())
            .<String>map(UserPrincipal::getId)
            .flatMap(userId -> {
                log.debug("Clearing cart for user: {}", userId);
                return cartService.clearCart(userId)
                    .then(Mono.just(ResponseEntity.ok().build()));
            });
    }

    @Operation(
        summary = "Get cart summary",
        description = "Get a summary of the cart including total items and price"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved cart summary"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated"),
        @ApiResponse(responseCode = "404", description = "Cart not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary")
    public Mono<ResponseEntity<CartSummaryResponse>> getCartSummary() {
        return ReactiveSecurityContextHolder.getContext()
            .<Authentication>map(SecurityContext::getAuthentication)
            .<UserPrincipal>map(auth -> (UserPrincipal) auth.getPrincipal())
            .<String>map(UserPrincipal::getId)
            .flatMap(userId -> {
                log.debug("Getting cart summary for user: {}", userId);
                return cartService.getCartSummary(userId)
                    .map(summary -> ResponseEntity.ok(CartSummaryResponse.fromDomain(summary)))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
            });
    }
}
