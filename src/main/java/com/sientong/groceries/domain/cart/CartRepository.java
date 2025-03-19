package com.sientong.groceries.domain.cart;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartRepository {
    Mono<Cart> findByUserId(String userId);
    Mono<Cart> save(Cart cart);
    Mono<CartItem> saveCartItem(CartItem item);
    Mono<Void> deleteCartItem(String itemId);
    Mono<Void> deleteAllCartItems(String cartId);
    Flux<CartItem> findItemsByCartId(String cartId);
}
