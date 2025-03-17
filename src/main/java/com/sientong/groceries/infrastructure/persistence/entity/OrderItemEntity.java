package com.sientong.groceries.infrastructure.persistence.entity;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.order.OrderItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("order_items")
public class OrderItemEntity {
    @Id
    private String id;
    private String orderId;
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public OrderItem toDomain() {
        return new OrderItem(
            productId,
            productName,
            Money.of(unitPrice),
            Quantity.of(quantity)
        );
    }

    public static OrderItemEntity fromDomain(String orderId, OrderItem item) {
        return OrderItemEntity.builder()
                .orderId(orderId)
                .productId(item.getProductId())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice().getAmount())
                .quantity(item.getQuantity().getValue())
                .subtotal(item.getSubtotal().getAmount())
                .build();
    }
}
