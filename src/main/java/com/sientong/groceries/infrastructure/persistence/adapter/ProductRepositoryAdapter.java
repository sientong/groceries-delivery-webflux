package com.sientong.groceries.infrastructure.persistence.adapter;

import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductCategory;
import com.sientong.groceries.domain.product.ProductRepository;
import com.sientong.groceries.domain.product.Quantity;
import com.sientong.groceries.infrastructure.persistence.entity.ProductEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {
    private final ReactiveProductRepository reactiveProductRepository;
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
    public Flux<Product> findByCategory(ProductCategory category) {
        return reactiveProductRepository.findByCategory(category)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Mono<Product> save(Product product) {
        return reactiveProductRepository.save(ProductEntity.fromDomain(product))
                .map(ProductEntity::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return reactiveProductRepository.deleteById(id);
    }

    @Override
    public Mono<Product> updateStock(String id, Quantity quantity) {
        return findById(id)
                .flatMap(product -> {
                    int newQuantity = product.getQuantity().getValue() + quantity.getValue();
                    return reactiveProductRepository.updateStock(id, newQuantity)
                            .filter(updated -> updated)
                            .flatMap(updated -> reactiveProductRepository.findById(id))
                            .map(ProductEntity::toDomain);
                });
    }

    public Flux<Product> findAvailableProducts() {
        return reactiveProductRepository.findAvailableProducts()
                .map(ProductEntity::toDomain);
    }

    public Flux<Product> findAvailableByCategory(ProductCategory category) {
        return reactiveProductRepository.findAvailableByCategory(category)
                .map(ProductEntity::toDomain);
    }

    public Flux<Product> findLowStockProducts() {
        return reactiveProductRepository.findLowStockProducts(LOW_STOCK_THRESHOLD)
                .map(ProductEntity::toDomain);
    }

    public Flux<Product> findOutOfStockProducts() {
        return reactiveProductRepository.findOutOfStockProducts()
                .map(ProductEntity::toDomain);
    }
}
