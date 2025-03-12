package com.sientong.groceries.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.sientong.groceries.api.controller.ProductController;
import com.sientong.groceries.api.request.StockUpdateRequest;
import com.sientong.groceries.domain.product.Money;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductCategory;
import com.sientong.groceries.domain.product.ProductService;
import com.sientong.groceries.domain.product.Quantity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @Test
    void shouldGetAllProducts() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(ProductCategory.FRUITS)
                .quantity(Quantity.of(100))
                .build();

        when(productService.findAll())
                .thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/v1/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(1)
                .contains(product);
    }

    @Test
    void shouldCreateProduct() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(ProductCategory.FRUITS)
                .quantity(Quantity.of(100))
                .build();

        when(productService.createProduct(any(Product.class)))
                .thenReturn(Mono.just(product));

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(product)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Product.class)
                .isEqualTo(product);
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

        when(productService.findByCategory(ProductCategory.FRUITS))
                .thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/v1/products?category=FRUITS")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(1)
                .contains(product);
    }

    @Test
    void shouldUpdateProductStock() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(ProductCategory.FRUITS)
                .quantity(Quantity.of(150))
                .build();

        when(productService.updateStock("1", Quantity.of(50)))
                .thenReturn(Mono.just(product));

        webTestClient.patch()
                .uri("/api/v1/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new StockUpdateRequest(Quantity.of(50)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(product);
    }
}
