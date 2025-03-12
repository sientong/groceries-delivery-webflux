package com.sientong.groceries.domain.product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<Product> findById(String id);
    Flux<Product> findAll();
    Flux<Product> findByCategory(ProductCategory category);
    Mono<Product> save(Product product);
    Mono<Void> deleteById(String id);
    Mono<Product> updateStock(String id, Quantity quantity);
    Flux<Product> findAvailableProducts();
    Flux<Product> findAvailableByCategory(ProductCategory category);
    Flux<Product> findLowStockProducts();
    Flux<Product> findOutOfStockProducts();
}
