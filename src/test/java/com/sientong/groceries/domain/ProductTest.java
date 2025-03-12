package com.sientong.groceries.domain;

import org.junit.jupiter.api.Test;

import com.sientong.groceries.domain.product.Money;
import com.sientong.groceries.domain.product.ProductCategory;
import com.sientong.groceries.domain.product.Quantity;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

import com.sientong.groceries.domain.product.Product;

class ProductTest {
    @Test
    void shouldCreateValidProduct() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .description("Fresh organic apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(ProductCategory.FRUITS)
                .quantity(Quantity.of(100))
                .build();

        assertNotNull(product);
        assertEquals("Organic Apples", product.getName());
        assertEquals(Money.of(BigDecimal.valueOf(5.99)), product.getPrice());
        assertEquals(ProductCategory.FRUITS, product.getCategory());
        assertEquals(100, product.getQuantity().getValue());
    }

    @Test
    void shouldNotCreateProductWithNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> 
            Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(-5.99)))
                .build()
        );
    }

    @Test
    void shouldNotCreateProductWithNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .quantity(Quantity.of(-1))
                .build()
        );
    }

    @Test
    void shouldUpdateStockCorrectly() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .quantity(Quantity.of(100))
                .build();

        product.updateStock(Quantity.of(50));
        assertEquals(50, product.getQuantity().getValue());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStockBelowZero() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .quantity(Quantity.of(100))
                .build();

        assertThrows(IllegalArgumentException.class, () ->
            product.updateStock(Quantity.of(-150))
        );
    }
}
