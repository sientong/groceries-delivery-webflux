package com.sientong.groceries.domain.cart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductService productService;

    @Override
    public Mono<Cart> getCart(String userId) {
        log.debug("Getting cart for user: {}", userId);
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("No cart found for user: {}, creating new cart", userId);
                    return createEmptyCart(userId);
                }));
    }

    @Override
    public Mono<Cart> addToCart(String userId, CartItem item) {
        log.debug("Adding item to cart for user: {}", userId);
        return getCart(userId)
                .flatMap(cart -> productService.findById(item.getProductId())
                        .map(product -> enrichCartItem(item, product))
                        .map(enrichedItem -> {
                            cart.addItem(enrichedItem);
                            return cart;
                        })
                        .switchIfEmpty(Mono.error(new RuntimeException("Product not found"))))
                .doOnError(ex -> log.error("Error adding item to cart", ex))
                .flatMap(cartRepository::save)
                .doOnError(ex -> log.error("Error saving cart", ex));
    }

    @Override
    public Mono<Cart> updateCartItem(String userId, String itemId, CartItem updatedItem) {
        log.debug("Updating item {} in cart for user: {}", itemId, userId);
        return getCart(userId)
                .map(cart -> {
                    cart.updateItem(itemId, updatedItem);
                    return cart;
                })
                .flatMap(cartRepository::save)
                .doOnError(ex -> log.error("Error updating cart item", ex));
    }

    @Override
    public Mono<Cart> removeItem(String userId, String itemId) {
        log.debug("Removing item {} from cart for user: {}", itemId, userId);
        return getCart(userId)
                .map(cart -> {
                    cart.removeItem(itemId);
                    return cart;
                })
                .flatMap(cartRepository::save)
                .doOnError(ex -> log.error("Error removing item from cart", ex));
    }

    @Override
    public Mono<Cart> clearCart(String userId) {
        log.debug("Clearing cart for user: {}", userId);
        return getCart(userId)
                .map(Cart::clear)
                .flatMap(cartRepository::save)
                .doOnError(ex -> log.error("Error clearing cart", ex));
    }

    @Override
    public Mono<CartSummary> getCartSummary(String userId) {
        log.debug("Getting cart summary for user: {}", userId);
        return getCart(userId)
                .map(cart -> CartSummary.builder()
                        .totalItems(cart.getItems().size())
                        .total(cart.getTotal())
                        .build());
    }

    private Mono<Cart> createEmptyCart(String userId) {
        Cart cart = Cart.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();
        return Mono.defer(() -> cartRepository.save(cart));
    }

    private CartItem enrichCartItem(CartItem item, Product product) {
        return CartItem.builder()
                .id(UUID.randomUUID().toString())
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
