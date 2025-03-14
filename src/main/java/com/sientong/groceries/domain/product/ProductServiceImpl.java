package com.sientong.groceries.domain.product;

import com.sientong.groceries.infrastructure.persistence.entity.ProductEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ReactiveProductRepository productRepository;

    @Override
    public Mono<Product> findById(String id) {
        return productRepository.findById(id)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Flux<Product> findAll() {
        return productRepository.findAll()
                .map(ProductEntity::toDomain);
    }

    @Override
    public Flux<Product> findByCategory(String categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Mono<Product> updateStock(String id, Quantity quantity) {
        return productRepository.findById(id)
                .map(entity -> {
                    entity.setQuantity(quantity.getValue());
                    entity.setUnit(quantity.getUnit());
                    return entity;
                })
                .flatMap(productRepository::save)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        ProductEntity entity = ProductEntity.fromDomain(product);
        return productRepository.save(entity)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Mono<Product> updateProduct(String id, Product product) {
        return productRepository.findById(id)
                .map(entity -> {
                    entity.setName(product.getName());
                    entity.setDescription(product.getDescription());
                    entity.setPrice(product.getPrice().getAmount());
                    entity.setCategoryId(product.getCategory().getId());
                    entity.setQuantity(product.getQuantity().getValue());
                    return entity;
                })
                .flatMap(productRepository::save)
                .map(ProductEntity::toDomain);
    }

    @Override
    public Mono<Void> deleteProduct(String id) {
        return productRepository.deleteById(id);
    }
}
