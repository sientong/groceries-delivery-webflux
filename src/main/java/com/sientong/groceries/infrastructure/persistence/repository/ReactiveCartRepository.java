package com.sientong.groceries.infrastructure.persistence.repository;

import com.sientong.groceries.infrastructure.persistence.entity.CartEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveCartRepository extends ReactiveCrudRepository<CartEntity, String> {
    Mono<CartEntity> findByUserId(String userId);
}
