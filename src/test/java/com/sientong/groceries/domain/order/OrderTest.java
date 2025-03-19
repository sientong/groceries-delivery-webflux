package com.sientong.groceries.domain.order;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;

class OrderTest {
    private Order order;
    private List<OrderItem> items;
    private DeliveryInfo deliveryInfo;

    @BeforeEach
    void setUp() {
        items = new ArrayList<>();
        items.add(OrderItem.of(
                "prod1",
                "Apple",
                Money.of(new BigDecimal("1.50"), "USD"),
                Quantity.of(2, "kg")));

        items.add(OrderItem.of(
                "prod2",
                "Orange",
                Money.of(new BigDecimal("2.00"), "USD"),
                Quantity.of(3, "kg")));

        deliveryInfo = DeliveryInfo.of(
                "123 Main St",
                "123-456-7890",
                "TRK123",
                LocalDateTime.now().plusDays(2),
                "Leave at door");

        order = Order.builder()
                .id("order1")
                .userId("user1")
                .items(items)
                .total(Money.of(new BigDecimal("9.00"), "USD"))
                .status(OrderStatus.PENDING)
                .deliveryInfo(deliveryInfo)
                .build();
    }

    @Test
    void shouldCreateOrderWithValidData() {
        assertNotNull(order.getId());
        assertEquals("user1", order.getUserId());
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("9.00"), order.getTotal().getAmount());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionForNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> {
            Order.builder()
                    .id("order1")
                    .userId(null)
                    .items(items)
                    .total(Money.of(new BigDecimal("9.00"), "USD"))
                    .build();
        });
    }

    @Test
    void shouldThrowExceptionForEmptyItems() {
        assertThrows(IllegalArgumentException.class, () -> {
            Order.builder()
                    .id("order1")
                    .userId("user1")
                    .items(new ArrayList<>())
                    .total(Money.of(new BigDecimal("0.00"), "USD"))
                    .build();
        });
    }

    @Test
    void shouldUpdateOrderStatus() {
        order.updateStatus(OrderStatus.PREPARING);
        
        assertEquals(OrderStatus.PREPARING, order.getStatus());
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()));
    }

    @Test
    void shouldNotUpdateStatusOfDeliveredOrder() {
        order.updateStatus(OrderStatus.DELIVERED);
        
        assertThrows(IllegalStateException.class, () -> {
            order.updateStatus(OrderStatus.PREPARING);
        });
    }

    @Test
    void shouldNotUpdateStatusOfCancelledOrder() {
        order.updateStatus(OrderStatus.CANCELLED);
        
        assertThrows(IllegalStateException.class, () -> {
            order.updateStatus(OrderStatus.PREPARING);
        });
    }

    @Test
    void shouldUpdateDeliveryInfo() {
        DeliveryInfo newDeliveryInfo = DeliveryInfo.of(
                "456 Oak St",
                "987-654-3210",
                "TRK456",
                LocalDateTime.now().plusDays(1),
                "Ring doorbell");

        order.updateDeliveryInfo(newDeliveryInfo);
        
        assertEquals("456 Oak St", order.getDeliveryInfo().getAddress());
        assertEquals("987-654-3210", order.getDeliveryInfo().getPhone());
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()));
    }

    @Test
    void shouldNotUpdateDeliveryInfoOfDeliveredOrder() {
        order.updateStatus(OrderStatus.DELIVERED);
        
        assertThrows(IllegalStateException.class, () -> {
            order.updateDeliveryInfo(DeliveryInfo.of(
                    "456 Oak St",
                    "987-654-3210",
                    "TRK456",
                    LocalDateTime.now().plusDays(1),
                    "Ring doorbell"));
        });
    }

    @Test
    void shouldCancelOrder() {
        order.cancel();
        
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()));
    }

    @Test
    void shouldNotCancelDeliveredOrder() {
        order.updateStatus(OrderStatus.DELIVERED);
        
        assertThrows(IllegalStateException.class, () -> {
            order.cancel();
        });
    }

    @Test
    void shouldNotCancelAlreadyCancelledOrder() {
        order.cancel();
        
        assertThrows(IllegalStateException.class, () -> {
            order.cancel();
        });
    }

    @Test
    void shouldCalculateTotalAutomaticallyIfNotProvided() {
        Order orderWithoutTotal = Order.builder()
                .id("order2")
                .userId("user1")
                .items(items)
                .build();
        
        assertEquals(new BigDecimal("9.00"), orderWithoutTotal.getTotal().getAmount());
    }
}
