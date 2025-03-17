package com.sientong.groceries.domain.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.infrastructure.persistence.entity.ProductEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ProductServiceTest {
    @Mock
    private ReactiveProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    void findById_ShouldReturnProduct() {
        // Given
        String productId = "1";
        Category category = Category.of("cat1", "Fruits");
        ProductEntity entity = ProductEntity.builder()
                .id(productId)
                .name("Apple")
                .description("Fresh apple")
                .categoryId(category.getId())
                .categoryName(category.getName())
                .price(BigDecimal.valueOf(1.99))
                .currency("USD")
                .quantity(100)
                .unit("piece")
                .imageUrl("http://example.com/apple.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepository.findById(productId)).thenReturn(Mono.just(entity));

        // When
        Mono<Product> result = productService.findById(productId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(product -> 
                    product.getId().equals(productId) &&
                    product.getName().equals("Apple") &&
                    product.getCategory().getId().equals(category.getId()) &&
                    product.getCategory().getName().equals(category.getName()) &&
                    product.getImageUrl().equals("http://example.com/apple.jpg")
                )
                .verifyComplete();
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        // Given
        Category category = Category.of("cat1", "Fruits");
        ProductEntity entity = ProductEntity.builder()
                .id("1")
                .name("Apple")
                .description("Fresh apple")
                .categoryId(category.getId())
                .categoryName(category.getName())
                .price(BigDecimal.valueOf(1.99))
                .currency("USD")
                .quantity(100)
                .unit("piece")
                .imageUrl("http://example.com/apple.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepository.findAll()).thenReturn(Flux.just(entity));

        // When
        Flux<Product> result = productService.findAll();

        // Then
        StepVerifier.create(result)
                .expectNextMatches(product -> 
                    product.getName().equals("Apple") &&
                    product.getCategory().getId().equals(category.getId()) &&
                    product.getCategory().getName().equals(category.getName()) &&
                    product.getImageUrl().equals("http://example.com/apple.jpg")
                )
                .verifyComplete();
    }

    @Test
    void createProduct_ShouldCreateAndReturnProduct() {
        // Given
        Category category = Category.of("cat1", "Fruits");
        Product product = new Product(
            null,
            "Apple",
            "Fresh apple",
            Money.of(BigDecimal.valueOf(1.99), "USD"),
            category,
            Quantity.of(100, "piece"),
            "http://example.com/apple.jpg",
            null,
            null
        );

        ProductEntity savedEntity = ProductEntity.builder()
                .id("1")
                .name("Apple")
                .description("Fresh apple")
                .categoryId(category.getId())
                .categoryName(category.getName())
                .price(BigDecimal.valueOf(1.99))
                .currency("USD")
                .quantity(100)
                .unit("piece")
                .imageUrl("http://example.com/apple.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepository.save(any(ProductEntity.class))).thenReturn(Mono.just(savedEntity));

        // When
        Mono<Product> result = productService.createProduct(product);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(savedProduct -> 
                    savedProduct.getName().equals("Apple") &&
                    savedProduct.getCategory().getId().equals(category.getId()) &&
                    savedProduct.getCategory().getName().equals(category.getName()) &&
                    savedProduct.getImageUrl().equals("http://example.com/apple.jpg")
                )
                .verifyComplete();
    }
}
