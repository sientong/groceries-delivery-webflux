package com.sientong.groceries.domain.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;

class ProductTest {
    private Category testCategory;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testCategory = Category.of("fruits", "Fruits");
        testTime = LocalDateTime.of(2025, 3, 14, 9, 35);
    }

    @Test
    void shouldCreateValidProduct() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .description("Fresh organic apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(100, "kg"))
                .createdAt(testTime)
                .build();

        assertNotNull(product);
        assertEquals("1", product.getId());
        assertEquals("Organic Apples", product.getName());
        assertEquals("Fresh organic apples", product.getDescription());
        assertEquals(Money.of(BigDecimal.valueOf(5.99)), product.getPrice());
        assertEquals(testCategory, product.getCategory());
        assertEquals(100, product.getQuantity().getValue());
        assertEquals("kg", product.getQuantity().getUnit());
        assertEquals(testTime, product.getCreatedAt());
    }

    @Test
    void shouldNotCreateProductWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> 
            Product.builder()
                .id("1")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(100))
                .build()
        );
    }

    @Test
    void shouldNotCreateProductWithNullPrice() {
        assertThrows(IllegalArgumentException.class, () -> 
            Product.builder()
                .id("1")
                .name("Organic Apples")
                .category(testCategory)
                .quantity(Quantity.of(100))
                .build()
        );
    }

    @Test
    void shouldNotCreateProductWithNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> 
            Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(-5.99)))
                .category(testCategory)
                .quantity(Quantity.of(100))
                .build()
        );
    }

    @Test
    void shouldNotCreateProductWithNullCategory() {
        assertThrows(IllegalArgumentException.class, () -> 
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
        assertThrows(IllegalArgumentException.class, () ->
            Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
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
                .category(testCategory)
                .quantity(Quantity.of(150, "kg"))
                .build();

        product.updateStock(Quantity.of(50, "kg"));
        assertEquals(50, product.getQuantity().getValue());
        assertEquals("kg", product.getQuantity().getUnit());
    }

    @Test
    void shouldAddStockCorrectly() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(100, "kg"))
                .build();

        product.addStock(Quantity.of(50, "kg"));
        assertEquals(150, product.getQuantity().getValue());
        assertEquals("kg", product.getQuantity().getUnit());
    }

    @Test
    void shouldRemoveStockCorrectly() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(100, "kg"))
                .build();

        product.removeStock(Quantity.of(50, "kg"));
        assertEquals(50, product.getQuantity().getValue());
        assertEquals("kg", product.getQuantity().getUnit());
    }

    @Test
    void shouldThrowExceptionWhenRemovingTooMuchStock() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(100, "kg"))
                .build();

        assertThrows(IllegalArgumentException.class, () ->
            product.removeStock(Quantity.of(150, "kg"))
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStockWithNegativeValue() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(100, "kg"))
                .build();

        assertThrows(IllegalArgumentException.class, () ->
            product.updateStock(Quantity.of(-1, "kg"))
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingStockBelowZero() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(100, "kg"))
                .build();

        assertThrows(IllegalArgumentException.class, () ->
            product.updateStock(Quantity.of(-150, "kg"))
        );
    }

    @Test
    void shouldFormatMoneyWithCurrencyCorrectly() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(100, "kg"))
                .build();

        assertEquals("$5.99 USD", product.getPrice().formatWithCurrency());
    }

    @Test
    void shouldFormatQuantityWithUnitCorrectly() {
        Product product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(1, "kg"))
                .build();

        assertEquals("1 kg", product.getQuantity().toString());

        product = Product.builder()
                .id("1")
                .name("Organic Apples")
                .price(Money.of(BigDecimal.valueOf(5.99)))
                .category(testCategory)
                .quantity(Quantity.of(2, "piece"))
                .build();

        assertEquals("2 pieces", product.getQuantity().toString());
    }
}
