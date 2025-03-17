package com.sientong.groceries.domain.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sientong.groceries.domain.common.Money;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Order {
    private final String id;
    private final String userId;
    private final List<OrderItem> items;
    private final Money total;
    private OrderStatus status;
    private DeliveryInfo deliveryInfo;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static class OrderBuilder {
        private OrderStatus status = OrderStatus.PENDING;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
    }

    public Order(String id, String userId, List<OrderItem> items, Money total, 
                OrderStatus status, DeliveryInfo deliveryInfo, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.total = total != null ? total : calculateTotal(items);
        this.status = status != null ? status : OrderStatus.PENDING;
        this.deliveryInfo = deliveryInfo;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void updateStatus(OrderStatus newStatus) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot update status of a " + this.status + " order");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDeliveryInfo(DeliveryInfo newDeliveryInfo) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot update delivery info of a " + this.status + " order");
        }
        if (newDeliveryInfo == null) {
            throw new IllegalArgumentException("Delivery info cannot be null");
        }
        this.deliveryInfo = newDeliveryInfo;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered order");
        }
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    private Money calculateTotal(List<OrderItem> items) {
        return Money.of(items.stream()
                .map(item -> item.getSubtotal().getAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
    }
}
