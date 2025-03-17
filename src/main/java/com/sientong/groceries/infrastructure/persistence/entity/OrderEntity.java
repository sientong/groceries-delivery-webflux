package com.sientong.groceries.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.order.DeliveryInfo;
import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class OrderEntity {
    @Id
    private String id;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String deliveryPhone;
    private String trackingNumber;
    private LocalDateTime estimatedDeliveryTime;
    private String deliveryNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private List<OrderItemEntity> items;

    public Order toDomain() {
        return Order.builder()
                .id(id)
                .userId(userId)
                .items(items.stream().map(OrderItemEntity::toDomain).toList())
                .total(Money.of(totalAmount))
                .status(status)
                .deliveryInfo(deliveryAddress != null ? DeliveryInfo.of(
                    deliveryAddress,
                    deliveryPhone,
                    trackingNumber,
                    estimatedDeliveryTime,
                    deliveryNotes
                ) : null)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static OrderEntity fromDomain(Order order) {
        return OrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotal().getAmount())
                .deliveryAddress(order.getDeliveryInfo() != null ? order.getDeliveryInfo().getAddress() : null)
                .deliveryPhone(order.getDeliveryInfo() != null ? order.getDeliveryInfo().getPhone() : null)
                .trackingNumber(order.getDeliveryInfo() != null ? order.getDeliveryInfo().getTrackingNumber() : null)
                .estimatedDeliveryTime(order.getDeliveryInfo() != null ? order.getDeliveryInfo().getEstimatedDeliveryTime() : null)
                .deliveryNotes(order.getDeliveryInfo() != null ? order.getDeliveryInfo().getDeliveryNotes() : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
