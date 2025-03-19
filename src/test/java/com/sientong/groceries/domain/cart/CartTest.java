package com.sientong.groceries.domain.cart;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sientong.groceries.domain.common.Money;

class CartTest {
    private Cart cart;
    private CartItem item1;
    private CartItem item2;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .id("cart1")
                .userId("user1")
                .items(new ArrayList<>())
                .total(Money.of(BigDecimal.ZERO))
                .updatedAt(LocalDateTime.now())
                .build();

        item1 = CartItem.builder()
                .id("item1")
                .productId("prod1")
                .name("Apple")
                .description("Fresh apple")
                .price(Money.of(new BigDecimal("1.50"), "USD"))
                .quantity(2)
                .unit("kg")
                .build();

        item2 = CartItem.builder()
                .id("item2")
                .productId("prod2")
                .name("Orange")
                .description("Fresh orange")
                .price(Money.of(new BigDecimal("2.00"), "USD"))
                .quantity(3)
                .unit("kg")
                .build();
    }

    @Test
    void shouldAddItemAndRecalculateTotal() {
        cart.addItem(item1);
        
        assertEquals(1, cart.getItems().size());
        assertEquals(new BigDecimal("3.00"), cart.getTotal().getAmount());
        assertEquals("USD", cart.getTotal().getCurrency());
    }

    @Test
    void shouldUpdateItemQuantityAndRecalculateTotal() {
        cart.addItem(item1);
        
        CartItem updatedItem = CartItem.builder()
                .id("item1")
                .productId("prod1")
                .name("Apple")
                .description("Fresh apple")
                .price(Money.of(new BigDecimal("1.50"), "USD"))
                .quantity(4)
                .unit("kg")
                .build();

        cart.updateItem("item1", updatedItem);
        
        assertEquals(1, cart.getItems().size());
        assertEquals(4, cart.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("6.00"), cart.getTotal().getAmount());
    }

    @Test
    void shouldRemoveItemAndRecalculateTotal() {
        cart.addItem(item1);
        cart.addItem(item2);
        
        cart.removeItem("item1");
        
        assertEquals(1, cart.getItems().size());
        assertEquals(new BigDecimal("6.00"), cart.getTotal().getAmount());
    }

    @Test
    void shouldClearCartAndResetTotal() {
        cart.addItem(item1);
        cart.addItem(item2);
        
        cart.clear();
        
        assertTrue(cart.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, cart.getTotal().getAmount());
    }

    @Test
    void shouldCalculateCorrectSubtotal() {
        cart.addItem(item1); // 1.50 * 2 = 3.00
        cart.addItem(item2); // 2.00 * 3 = 6.00
        
        assertEquals(new BigDecimal("9.00"), cart.getTotal().getAmount());
    }

    @Test
    void shouldUpdateTimestampOnCartModification() {
        LocalDateTime beforeUpdate = cart.getUpdatedAt();
        cart.addItem(item1);
        
        assertTrue(cart.getUpdatedAt().isAfter(beforeUpdate));
    }
}
