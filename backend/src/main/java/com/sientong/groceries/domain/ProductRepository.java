package com.sientong.groceries.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Flux<Product> findAll();
    Mono<Product> findById(String id);
    Mono<Product> save(Product product);
    Mono<Void> deleteById(String id);
    Flux<Product> findByCategory(ProductCategory category);
    Mono<Product> updateStock(String id, int quantityDelta);
}
