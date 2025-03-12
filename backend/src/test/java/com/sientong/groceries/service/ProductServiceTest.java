package com.sientong.groceries.service;

import com.sientong.groceries.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository);
    }

    @Test
    void shouldCreateProduct() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .description("Fresh organic apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(ProductCategory.FRUITS)
                .quantity(Quantity.of(100))
                .build();

        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(product));

        StepVerifier.create(productService.createProduct(product))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void shouldGetAllProducts() {
        Product product1 = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .quantity(Quantity.of(100))
                .build();

        Product product2 = Product.builder()
                .id("2")
                .name("Organic Bananas")
                .price(Money.of(BigDecimal.valueOf(4.99)))
                .quantity(Quantity.of(150))
                .build();

        when(productRepository.findAll())
                .thenReturn(Flux.just(product1, product2));

        StepVerifier.create(productService.getAllProducts())
                .expectNext(product1, product2)
                .verifyComplete();
    }

    @Test
    void shouldUpdateProductStock() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .quantity(Quantity.of(100))
                .build();

        when(productRepository.updateStock("1", 50))
                .thenReturn(Mono.just(product));

        StepVerifier.create(productService.updateStock("1", 50))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void shouldGetProductsByCategory() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(ProductCategory.FRUITS)
                .quantity(Quantity.of(100))
                .build();

        when(productRepository.findByCategory(ProductCategory.FRUITS))
                .thenReturn(Flux.just(product));

        StepVerifier.create(productService.getProductsByCategory(ProductCategory.FRUITS))
                .expectNext(product)
                .verifyComplete();
    }
}
