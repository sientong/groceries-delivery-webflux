package com.sientong.groceries.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.sientong.groceries.api.controller.ProductController;
import com.sientong.groceries.api.request.ProductRequest;
import com.sientong.groceries.api.request.StockUpdateRequest;
import com.sientong.groceries.api.response.ProductResponse;
import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.product.Category;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ProductController.class)
class ProductControllerTest {

    @MockBean
    private ProductService productService;

    @Autowired
    private WebTestClient webTestClient;

    private Category createTestCategory() {
        return Category.of("fruits", "Fruits");
    }

    private Product createTestProduct() {
        Category category = createTestCategory();
        return Product.builder()
                .id("1")
                .name("Organic Apples")
                .description("Fresh organic apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(100, "kg"))
                .createdAt(LocalDateTime.of(2025, 3, 13, 0, 0))
                .updatedAt(LocalDateTime.of(2025, 3, 13, 0, 0))
                .build();
    }

    @Test
    void shouldGetAllProducts() {
        Product product = createTestProduct();
        when(productService.findAll()).thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/v1/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponse.class)
                .hasSize(1);
    }

    @Test
    void shouldGetProductById() {
        Product product = createTestProduct();
        when(productService.findById("1")).thenReturn(Mono.just(product));

        webTestClient.get()
                .uri("/api/v1/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class);
    }

    @Test
    void shouldGetProductsByCategory() {
        Product product = createTestProduct();
        Category category = createTestCategory();
        when(productService.findByCategory(category.getId())).thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/v1/products/category/" + category.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponse.class)
                .hasSize(1);
    }

    @Test
    void shouldUpdateStock() {
        Product product = createTestProduct();
        product = Product.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .quantity(Quantity.of(150, "kg"))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

        StockUpdateRequest request = new StockUpdateRequest();
        request.setQuantity(150);

        when(productService.updateStock("1", Quantity.of(150, "kg"))).thenReturn(Mono.just(product));

        webTestClient.patch()
                .uri("/api/v1/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class);
    }

    @Test
    void shouldCreateProduct() {
        Product product = createTestProduct();
        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice().getAmount());
        request.setCategory(product.getCategory());
        request.setQuantity(product.getQuantity().getValue());
        request.setUnit(product.getQuantity().getUnit());

        when(productService.createProduct(any(Product.class))).thenReturn(Mono.just(product));

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponse.class);
    }

    @Test
    void shouldReturn404WhenProductNotFound() {
        when(productService.findById("nonexistent")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/products/nonexistent")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn400WhenInvalidStockUpdate() {
        StockUpdateRequest request = new StockUpdateRequest();
        request.setQuantity(-1);

        webTestClient.patch()
                .uri("/api/v1/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
