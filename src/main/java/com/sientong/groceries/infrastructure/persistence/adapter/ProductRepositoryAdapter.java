package com.sientong.groceries.infrastructure.persistence.adapter;

import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductRepository;
import com.sientong.groceries.domain.product.Quantity;
import com.sientong.groceries.infrastructure.persistence.entity.ProductEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {
    private final ReactiveProductRepository reactiveProductRepository;
    private final DatabaseClient databaseClient;
    private static final int LOW_STOCK_THRESHOLD = 5;

    @Override
    public Mono<Product> findById(String id) {
        return reactiveProductRepository.findById(id)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Flux<Product> findAll() {
        return reactiveProductRepository.findAll()
                .map(ProductEntity::toDomain);
    }

    @Override
    public Flux<Product> findByCategory(String categoryId) {
        return reactiveProductRepository.findByCategoryId(categoryId)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Mono<Product> save(Product product) {
        ProductEntity entity = ProductEntity.fromDomain(product);
        return reactiveProductRepository.save(entity)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return reactiveProductRepository.deleteById(id);
    }

    @Override
    public Mono<Product> updateStock(String id, Quantity quantity) {
        return databaseClient.sql("UPDATE products SET quantity = :quantity, updated_at = CURRENT_TIMESTAMP WHERE id = :id AND :quantity >= 0")
                .bind("id", id)
                .bind("quantity", quantity.getValue())
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> rowsUpdated > 0 ? reactiveProductRepository.findById(id).map(ProductEntity::toDomain) : Mono.empty());
    }

    @Override
    public Flux<Product> findAvailable() {
        return databaseClient.sql("SELECT * FROM products WHERE quantity > 0")
                .map((row, metadata) -> mapToProductEntity(row))
                .all()
                .map(ProductEntity::toDomain);
    }

    @Override
    public Flux<Product> findAvailableByCategory(String categoryId) {
        return databaseClient.sql("SELECT * FROM products WHERE category_id = :categoryId AND quantity > 0")
                .bind("categoryId", categoryId)
                .map((row, metadata) -> mapToProductEntity(row))
                .all()
                .map(ProductEntity::toDomain);
    }

    @Override
    public Flux<Product> findLowStockProducts() {
        return databaseClient.sql("SELECT * FROM products WHERE quantity <= :threshold")
                .bind("threshold", LOW_STOCK_THRESHOLD)
                .map((row, metadata) -> mapToProductEntity(row))
                .all()
                .map(ProductEntity::toDomain);
    }

    @Override
    public Flux<Product> findOutOfStockProducts() {
        return databaseClient.sql("SELECT * FROM products WHERE quantity = 0")
                .map((row, metadata) -> mapToProductEntity(row))
                .all()
                .map(ProductEntity::toDomain);
    }

    private ProductEntity mapToProductEntity(io.r2dbc.spi.Row row) {
        return ProductEntity.builder()
                .id(row.get("id", String.class))
                .name(row.get("name", String.class))
                .description(row.get("description", String.class))
                .categoryId(row.get("category_id", String.class))
                .categoryName(row.get("category_name", String.class))
                .price(row.get("price", java.math.BigDecimal.class))
                .currency(row.get("currency", String.class))
                .quantity(row.get("quantity", Integer.class))
                .unit(row.get("unit", String.class))
                .imageUrl(row.get("image_url", String.class))
                .createdAt(row.get("created_at", java.time.LocalDateTime.class))
                .updatedAt(row.get("updated_at", java.time.LocalDateTime.class))
                .build();
    }
}
