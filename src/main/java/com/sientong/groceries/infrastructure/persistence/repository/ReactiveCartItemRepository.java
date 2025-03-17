package com.sientong.groceries.infrastructure.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.sientong.groceries.infrastructure.persistence.entity.CartItemEntity;

import reactor.core.publisher.Flux;

@Repository
public interface ReactiveCartItemRepository extends ReactiveCrudRepository<CartItemEntity, String> {
    @Query("SELECT ci.id, ci.cart_id, ci.product_id, ci.name, ci.description, ci.price, ci.currency, ci.quantity, ci.unit FROM cart_items ci WHERE ci.cart_id = :cartId")
    Flux<CartItemEntity> findByCartId(String cartId);
}
