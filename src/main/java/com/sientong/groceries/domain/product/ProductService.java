package com.sientong.groceries.domain.product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<Product> findById(String id);
    Flux<Product> findAll();
    Flux<Product> findByCategory(ProductCategory category);
    Mono<Product> createProduct(Product product);
    Mono<Product> updateProduct(String id, Product product);
    Mono<Void> deleteProduct(String id);
    Mono<Product> updateStock(String id, Quantity quantity);
}
