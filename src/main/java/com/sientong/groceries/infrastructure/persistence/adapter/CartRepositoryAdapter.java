package com.sientong.groceries.infrastructure.persistence.adapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sientong.groceries.domain.cart.Cart;
import com.sientong.groceries.domain.cart.CartItem;
import com.sientong.groceries.domain.cart.CartRepository;
import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.infrastructure.persistence.entity.CartEntity;
import com.sientong.groceries.infrastructure.persistence.entity.CartItemEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveCartItemRepository;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveCartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {
    private final ReactiveCartRepository cartRepository;
    private final ReactiveCartItemRepository cartItemRepository;

    @Override
    public Mono<Cart> findByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(createEmptyCart(userId))
                .flatMap(cartEntity -> cartItemRepository.findByCartId(cartEntity.getId())
                        .defaultIfEmpty(CartItemEntity.builder().build())
                        .map(CartItemEntity::toDomain)
                        .collectList()
                        .map(items -> {
                            Cart cart = cartEntity.toDomain();
                            cart.setItems(items != null ? items : new ArrayList<>());
                            return cart;
                        }));
    }

    @Override
    public Mono<Cart> save(Cart cart) {
        CartEntity cartEntity = CartEntity.fromDomain(cart);
        return cartRepository.save(cartEntity)
                .flatMap(savedCart -> {
                    List<CartItemEntity> itemEntities = cart.getItems() != null ? cart.getItems().stream()
                            .map(item -> {
                                CartItemEntity entity = CartItemEntity.fromDomain(item);
                                entity.setCartId(savedCart.getId());
                                return entity;
                            })
                            .toList() : new ArrayList<>();
                    
                    if (itemEntities.isEmpty()) {
                        Cart emptyCart = savedCart.toDomain();
                        emptyCart.setItems(new ArrayList<>());
                        return Mono.just(emptyCart);
                    }

                    return cartItemRepository.saveAll(itemEntities)
                            .defaultIfEmpty(CartItemEntity.builder().build())
                            .map(CartItemEntity::toDomain)
                            .collectList()
                            .map(savedItems -> {
                                Cart savedCartWithItems = savedCart.toDomain();
                                savedCartWithItems.setItems(savedItems != null ? savedItems : new ArrayList<>());
                                return savedCartWithItems;
                            });
                });
    }

    @Override
    public Mono<CartItem> saveCartItem(CartItem item) {
        return cartItemRepository.save(CartItemEntity.fromDomain(item))
                .map(CartItemEntity::toDomain);
    }

    @Override
    public Mono<Void> deleteCartItem(String itemId) {
        return cartItemRepository.deleteById(itemId);
    }

    @Override
    public Mono<Void> deleteAllCartItems(String cartId) {
        return cartItemRepository.deleteByCartId(cartId);
    }

    @Override
    public Flux<CartItem> findItemsByCartId(String cartId) {
        return cartItemRepository.findByCartId(cartId)
                .defaultIfEmpty(CartItemEntity.builder().build())
                .map(CartItemEntity::toDomain);
    }

    private Mono<CartEntity> createEmptyCart(String userId) {
        log.debug("Creating new empty cart for user: {}", userId);
        CartEntity cart = CartEntity.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .total(Money.ZERO.getAmount())
                .currency(Money.ZERO.getCurrency())
                .updatedAt(LocalDateTime.now())
                .build();
        return cartRepository.save(cart);
    }
}
