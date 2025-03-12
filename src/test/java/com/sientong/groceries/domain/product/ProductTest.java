package com.sientong.groceries.domain.product;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    @Test
    void shouldCreateProductSuccessfully() {
        Product product = new Product(
            "1",
            "Apple",
            "Fresh red apple",
            Money.of(new java.math.BigDecimal("1.99")),
            ProductCategory.FRUITS,
            Quantity.of(100)
        );

        assertEquals("1", product.getId());
        assertEquals("Apple", product.getName());
        assertEquals("Fresh red apple", product.getDescription());
        assertEquals(new java.math.BigDecimal("1.99"), product.getPrice().getAmount());
        assertEquals(ProductCategory.FRUITS, product.getCategory());
        assertEquals(100, product.getQuantity().getValue());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Product(
            "1",
            null,
            "Description",
            Money.of(new java.math.BigDecimal("1.99")),
            ProductCategory.FRUITS,
            Quantity.of(100)
        ));
    }

    @Test
    void shouldThrowExceptionWhenPriceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Product(
            "1",
            "Apple",
            "Description",
            null,
            ProductCategory.FRUITS,
            Quantity.of(100)
        ));
    }

    @Test
    void shouldUpdateStockSuccessfully() {
        Product product = new Product(
            "1",
            "Apple",
            "Fresh red apple",
            Money.of(new java.math.BigDecimal("1.99")),
            ProductCategory.FRUITS,
            Quantity.of(100)
        );

        product.updateStock(Quantity.of(50));
        assertEquals(150, product.getQuantity().getValue());

        product.updateStock(Quantity.of(-30));
        assertEquals(120, product.getQuantity().getValue());
    }

    @Test
    void shouldThrowExceptionWhenStockBelowZero() {
        Product product = new Product(
            "1",
            "Apple",
            "Fresh red apple",
            Money.of(new java.math.BigDecimal("1.99")),
            ProductCategory.FRUITS,
            Quantity.of(100)
        );

        assertThrows(IllegalArgumentException.class, () -> product.updateStock(Quantity.of(-150)));
    }
}
