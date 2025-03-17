package com.sientong.groceries.domain.cart;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductService;
import com.sientong.groceries.infrastructure.persistence.entity.CartEntity;
import com.sientong.groceries.infrastructure.persistence.entity.CartItemEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveCartItemRepository;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveCartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final ReactiveCartRepository cartRepository;
    private final ReactiveCartItemRepository cartItemRepository;
    private final ProductService productService;

    @Override
    public Mono<Cart> getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .flatMap(cartEntity -> cartItemRepository.findByCartId(cartEntity.getId())
                        .collectList()
                        .map(items -> {
                            cartEntity.setItems(items);
                            return cartEntity.toDomain();
                        }))
                .switchIfEmpty(Mono.just(Cart.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .total(Money.ZERO)
                    .updatedAt(LocalDateTime.now())
                    .build()));
    }

    @Override
    public Mono<Cart> addToCart(String userId, CartItem item) {
        return getOrCreateCart(userId)
                .flatMap(cart -> productService.findById(item.getProductId())
                        .map(product -> enrichCartItem(item, product))
                        .flatMap(enrichedItem -> {
                            cart.addItem(enrichedItem);
                            return Mono.just(cart);
                        }))
                .flatMap(cart -> {
                    CartEntity cartEntity = CartEntity.fromDomain(cart);
                    return cartRepository.save(cartEntity)
                            .flatMap(savedCart -> Mono.when(
                                cartItemRepository.saveAll(cartEntity.getItems())
                                    .collectList()
                                    .doOnNext(savedCart::setItems)
                            ).then(Mono.just(savedCart.toDomain())));
                });
    }

    @Override
    public Mono<Cart> updateCartItem(String userId, String itemId, CartItem updatedItem) {
        return getOrCreateCart(userId)
                .map(cart -> {
                    cart.updateItem(itemId, updatedItem);
                    return cart;
                })
                .flatMap(cart -> {
                    CartEntity cartEntity = CartEntity.fromDomain(cart);
                    return cartRepository.save(cartEntity)
                            .flatMap(savedCart -> {
                                CartItemEntity itemToUpdate = cartEntity.getItems().stream()
                                    .filter(item -> item.getId().equals(itemId))
                                    .findFirst()
                                    .orElseThrow(() -> new IllegalStateException("Cart item not found"));
                                return cartItemRepository.save(itemToUpdate)
                                    .then(Mono.just(savedCart.toDomain()));
                            });
                });
    }

    @Override
    public Mono<Cart> removeCartItem(String userId, String itemId) {
        return getOrCreateCart(userId)
                .map(cart -> {
                    cart.removeItem(itemId);
                    return cart;
                })
                .flatMap(cart -> {
                    CartEntity cartEntity = CartEntity.fromDomain(cart);
                    return cartRepository.save(cartEntity)
                            .flatMap(savedCart -> cartItemRepository.deleteById(itemId)
                                    .then(Mono.just(savedCart.toDomain())));
                });
    }

    @Override
    public Mono<Void> clearCart(String userId) {
        return getOrCreateCart(userId)
                .map(cart -> {
                    cart.clear();
                    return cart;
                })
                .flatMap(cart -> {
                    CartEntity cartEntity = CartEntity.fromDomain(cart);
                    return cartRepository.save(cartEntity)
                            .flatMap(savedCart -> cartItemRepository.deleteAll()
                                    .then());
                });
    }

    @Override
    public Mono<CartSummary> getCartSummary(String userId) {
        return getCart(userId)
                .map(cart -> CartSummary.builder()
                        .totalItems(cart.getItems().size())
                        .total(cart.getTotal())
                        .build());
    }

    private Mono<Cart> getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .flatMap(cartEntity -> cartItemRepository.findByCartId(cartEntity.getId())
                        .collectList()
                        .map(items -> {
                            cartEntity.setItems(items);
                            return cartEntity.toDomain();
                        }))
                .switchIfEmpty(createEmptyCart(userId));
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
