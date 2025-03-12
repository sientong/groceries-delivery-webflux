package com.sientong.groceries.service;

import com.sientong.groceries.domain.Product;
import com.sientong.groceries.domain.ProductCategory;
import com.sientong.groceries.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Mono<Product> createProduct(Product product) {
        return productRepository.save(product);
    }

    public Flux<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Mono<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public Mono<Product> updateStock(String id, int quantityDelta) {
        return productRepository.updateStock(id, quantityDelta);
    }

    public Flux<Product> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    public Mono<Void> deleteProduct(String id) {
        return productRepository.deleteById(id);
    }
}
