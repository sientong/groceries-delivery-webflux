package com.sientong.groceries.domain.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;

class ProductTest {
    @Test
    void shouldCreateValidProduct() {
        LocalDateTime now = LocalDateTime.of(2025, 3, 14, 9, 35);
        Category category = Category.of("fruits", "Fruits");
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .description("Fresh organic apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(100, "kg"))
                .createdAt(now)
                .build();

        assertNotNull(product);
        assertEquals("1", product.getId());
        assertEquals("Organic Apples", product.getName());
        assertEquals("Fresh organic apples", product.getDescription());
        assertEquals(Money.of(BigDecimal.valueOf(5.99)), product.getPrice());
        assertEquals(category, product.getCategory());
        assertEquals(100, product.getQuantity().getValue());
        assertEquals("kg", product.getQuantity().getUnit());
        assertEquals(now, product.getCreatedAt());
    }

    @Test
    void shouldNotCreateProductWithNullName() {
        Category category = Category.of("fruits", "Fruits");
        assertThrows(NullPointerException.class, () -> 
            Product.builder()
                .id("1")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(100))
                .build()
        );
    }

    @Test
    void shouldNotCreateProductWithNullPrice() {
        Category category = Category.of("fruits", "Fruits");
        assertThrows(NullPointerException.class, () -> 
            Product.builder()
                .id("1")
                .name("Organic Apples")
                .category(category)
                .quantity(Quantity.of(100))
                .build()
        );
    }

    @Test
    void shouldNotCreateProductWithNegativePrice() {
        Category category = Category.of("fruits", "Fruits");
        assertThrows(IllegalArgumentException.class, () -> 
            Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(-5.99)))
                .category(category)
                .quantity(Quantity.of(100))
                .build()
        );
    }

    @Test
    void shouldNotCreateProductWithNullCategory() {
        assertThrows(NullPointerException.class, () -> 
            Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .quantity(Quantity.of(100))
                .build()
        );
    }

    @Test
    void shouldNotCreateProductWithNegativeQuantity() {
        Category category = Category.of("fruits", "Fruits");
        assertThrows(IllegalArgumentException.class, () ->
            Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(-1))
                .build()
        );
    }

    @Test
    void shouldUpdateStockCorrectly() {
        Category category = Category.of("fruits", "Fruits");
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(100, "kg"))
                .build();

        product.updateStock(Quantity.of(50, "kg"));
        assertEquals(50, product.getQuantity().getValue());
        assertEquals("kg", product.getQuantity().getUnit());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStockWithDifferentUnit() {
        Category category = Category.of("fruits", "Fruits");
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(100, "kg"))
                .build();

        assertThrows(IllegalArgumentException.class, () ->
            product.updateStock(Quantity.of(50, "piece"))
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStockBelowZero() {
        Category category = Category.of("fruits", "Fruits");
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(100, "kg"))
                .build();

        assertThrows(IllegalArgumentException.class, () ->
            product.updateStock(Quantity.of(-150, "kg"))
        );
    }

    @Test
    void shouldFormatMoneyWithCurrencyCorrectly() {
        Category category = Category.of("fruits", "Fruits");
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(100, "kg"))
                .build();

        assertEquals("$5.99 USD", product.getPrice().formatWithCurrency());
    }

    @Test
    void shouldFormatQuantityWithUnitCorrectly() {
        Category category = Category.of("fruits", "Fruits");
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(1, "kg"))
                .build();

        assertEquals("1 kg", product.getQuantity().toString());

        product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(category)
                .quantity(Quantity.of(2, "piece"))
                .build();

        assertEquals("2 pieces", product.getQuantity().toString());
    }
}
