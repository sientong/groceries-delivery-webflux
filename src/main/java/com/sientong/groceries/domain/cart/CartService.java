package com.sientong.groceries.domain.cart;

import reactor.core.publisher.Mono;

public interface CartService {
    /**
     * Get cart for a user. Always returns a cart, never empty.
     * If no cart exists, creates a new empty cart.
     * 
     * @param userId the user ID
     * @return a Mono containing the cart, never empty
     */
    Mono<Cart> getCart(String userId);

    /**
     * Add item to cart. If item already exists, updates quantity.
     * Always returns the updated cart.
     * 
     * @param userId the user ID
     * @param item the item to add
     * @return a Mono containing the updated cart, never empty
     */
    Mono<Cart> addToCart(String userId, CartItem item);

    /**
     * Update an item in a user's cart. If no cart exists, creates a new empty cart.
     * If the item doesn't exist, returns the cart unchanged.
     * 
     * @param userId the user ID
     * @param itemId the item ID to update
     * @param updatedItem the updated item details
     * @return a Mono containing the updated cart, never empty
     */
    Mono<Cart> updateCartItem(String userId, String itemId, CartItem updatedItem);

    /**
     * Remove item from cart.
     * Always returns the updated cart, even if empty.
     * 
     * @param userId the user ID
     * @param itemId the item ID to remove
     * @return a Mono containing the updated cart, never empty
     */
    Mono<Cart> removeItem(String userId, String itemId);

    /**
     * Clear all items from cart.
     * Returns an empty cart, never null.
     * 
     * @param userId the user ID
     * @return a Mono containing the empty cart, never empty
     */
    Mono<Cart> clearCart(String userId);

    /**
     * Get a summary of a user's cart. If no cart exists, returns a summary with zero items.
     * 
     * @param userId the user ID
     * @return a Mono containing the cart summary, never empty
     */
    Mono<CartSummary> getCartSummary(String userId);
}
