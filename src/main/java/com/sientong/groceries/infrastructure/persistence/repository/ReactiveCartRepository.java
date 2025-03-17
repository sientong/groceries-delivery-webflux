package com.sientong.groceries.infrastructure.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.sientong.groceries.infrastructure.persistence.entity.CartEntity;

import reactor.core.publisher.Mono;

@Repository
public interface ReactiveCartRepository extends ReactiveCrudRepository<CartEntity, String> {
    @Query("SELECT c.id, c.user_id, c.total, c.currency, c.updated_at FROM carts c WHERE c.user_id = :userId")
    Mono<CartEntity> findByUserId(String userId);
}
