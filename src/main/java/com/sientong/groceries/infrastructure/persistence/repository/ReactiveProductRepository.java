package com.sientong.groceries.infrastructure.persistence.repository;

import com.sientong.groceries.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReactiveProductRepository extends ReactiveCrudRepository<ProductEntity, String> {
    Flux<ProductEntity> findByCategoryId(String categoryId);
}
