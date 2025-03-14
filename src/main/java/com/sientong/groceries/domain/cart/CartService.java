package com.sientong.groceries.domain.cart;

import reactor.core.publisher.Mono;

public interface CartService {
    Mono<Cart> getCart(String userId);
    Mono<Cart> addToCart(String userId, CartItem item);
    Mono<Cart> updateCartItem(String userId, String itemId, CartItem item);
    Mono<Cart> removeCartItem(String userId, String itemId);
    Mono<Void> clearCart(String userId);
    Mono<CartSummary> getCartSummary(String userId);
}
