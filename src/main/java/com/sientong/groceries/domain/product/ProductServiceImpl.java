package com.sientong.groceries.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Mono<Product> findById(String id) {
        return productRepository.findById(id);
    }

    @Override
    public Flux<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Flux<Product> findByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Mono<Product> updateProduct(String id, Product product) {
        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found")))
            .flatMap(existingProduct -> {
                Product updatedProduct = new Product(
                    existingProduct.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getCategory(),
                    product.getQuantity()
                );
                return productRepository.save(updatedProduct);
            });
    }

    @Override
    public Mono<Void> deleteProduct(String id) {
        return productRepository.deleteById(id);
    }

    @Override
    public Mono<Product> updateStock(String id, Quantity quantity) {
        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found")))
            .flatMap(product -> productRepository.updateStock(id, quantity));
    }
}
