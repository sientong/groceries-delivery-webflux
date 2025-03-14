package com.sientong.groceries.domain.cart;

import com.sientong.groceries.domain.product.Money;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductService;
import com.sientong.groceries.infrastructure.persistence.entity.CartEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final ReactiveCartRepository cartRepository;
    private final ProductService productService;

    @Override
    public Mono<Cart> getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .map(CartEntity::toDomain)
                .switchIfEmpty(createEmptyCart(userId));
    }

    @Override
    public Mono<Cart> addToCart(String userId, CartItem item) {
        return getCart(userId)
                .flatMap(cart -> productService.findById(item.getProductId())
                        .map(product -> enrichCartItem(item, product))
                        .map(enrichedItem -> {
                            cart.addItem(enrichedItem);
                            return cart;
                        }))
                .map(CartEntity::fromDomain)
                .flatMap(cartRepository::save)
                .map(CartEntity::toDomain);
    }

    @Override
    public Mono<Cart> updateCartItem(String userId, String itemId, CartItem updatedItem) {
        return getCart(userId)
                .map(cart -> {
                    cart.updateItem(itemId, updatedItem);
                    return cart;
                })
                .map(CartEntity::fromDomain)
                .flatMap(cartRepository::save)
                .map(CartEntity::toDomain);
    }

    @Override
    public Mono<Cart> removeCartItem(String userId, String itemId) {
        return getCart(userId)
                .map(cart -> {
                    cart.removeItem(itemId);
                    return cart;
                })
                .map(CartEntity::fromDomain)
                .flatMap(cartRepository::save)
                .map(CartEntity::toDomain);
    }

    @Override
    public Mono<Void> clearCart(String userId) {
        return getCart(userId)
                .map(cart -> {
                    cart.clear();
                    return cart;
                })
                .map(CartEntity::fromDomain)
                .flatMap(cartRepository::save)
                .then();
    }

    @Override
    public Mono<CartSummary> getCartSummary(String userId) {
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
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();
        return Mono.just(cart)
                .map(CartEntity::fromDomain)
                .flatMap(cartRepository::save)
                .map(CartEntity::toDomain);
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
