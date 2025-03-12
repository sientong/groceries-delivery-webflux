package com.sientong.groceries.domain.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
        testProduct = new Product(
            "1",
            "Apple",
            "Fresh red apple",
            Money.of(new BigDecimal("1.99")),
            ProductCategory.FRUITS,
            Quantity.of(100)
        );
    }

    @Test
    void shouldFindProductById() {
        when(productRepository.findById("1")).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.findById("1"))
            .expectNext(testProduct)
            .verifyComplete();
    }

    @Test
    void shouldFindAllProducts() {
        when(productRepository.findAll()).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.findAll())
            .expectNext(testProduct)
            .verifyComplete();
    }

    @Test
    void shouldFindProductsByCategory() {
        when(productRepository.findByCategory(ProductCategory.FRUITS))
            .thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.findByCategory(ProductCategory.FRUITS))
            .expectNext(testProduct)
            .verifyComplete();
    }

    @Test
    void shouldCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.createProduct(testProduct))
            .expectNext(testProduct)
            .verifyComplete();
    }

    @Test
    void shouldUpdateStock() {
        Product updatedProduct = new Product(
            "1",
            "Apple",
            "Fresh red apple",
            Money.of(new BigDecimal("1.99")),
            ProductCategory.FRUITS,
            Quantity.of(150)
        );

        when(productRepository.findById("1")).thenReturn(Mono.just(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(updatedProduct));

        StepVerifier.create(productService.updateStock("1", Quantity.of(50)))
            .expectNext(updatedProduct)
            .verifyComplete();
    }
}
