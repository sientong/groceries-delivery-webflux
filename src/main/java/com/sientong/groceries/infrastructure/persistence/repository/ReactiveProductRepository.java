package com.sientong.groceries.infrastructure.persistence.repository;

import com.sientong.groceries.domain.product.ProductCategory;
import com.sientong.groceries.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveProductRepository extends ReactiveCrudRepository<ProductEntity, String> {
    Flux<ProductEntity> findByCategory(ProductCategory category);

    @Modifying
    @Query("UPDATE products SET quantity = :newQuantity, updated_at = CURRENT_TIMESTAMP WHERE id = :id AND :newQuantity >= 0")
    Mono<Boolean> updateStock(String id, int newQuantity);

    @Query("SELECT * FROM products WHERE quantity > 0")
    Flux<ProductEntity> findAvailableProducts();

    @Query("SELECT * FROM products WHERE category = :category AND quantity > 0")
    Flux<ProductEntity> findAvailableByCategory(ProductCategory category);

    @Query("SELECT * FROM products WHERE quantity <= :threshold")
    Flux<ProductEntity> findLowStockProducts(int threshold);

    @Query("SELECT * FROM products WHERE quantity = 0")
    Flux<ProductEntity> findOutOfStockProducts();
}
