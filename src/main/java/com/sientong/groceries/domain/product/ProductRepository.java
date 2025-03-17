package com.sientong.groceries.domain.product;

import com.sientong.groceries.domain.common.Quantity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<Product> findById(String id);
    Flux<Product> findAll();
    Flux<Product> findByCategory(String categoryId);
    Mono<Product> save(Product product);
    Mono<Void> deleteById(String id);
    Flux<Product> findAvailable();
    Flux<Product> findAvailableByCategory(String categoryId);
    Mono<Product> updateStock(String id, Quantity quantity);
    Flux<Product> findLowStockProducts();
    Flux<Product> findOutOfStockProducts();
}
