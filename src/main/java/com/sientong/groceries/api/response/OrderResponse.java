package com.sientong.groceries.api.response;

import com.sientong.groceries.domain.order.DeliveryInfo;
import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class OrderResponse {
    String id;
    String userId;
    List<OrderItemResponse> items;
    OrderStatus status;
    DeliveryInfo deliveryInfo;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static OrderResponse fromDomain(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderItemResponse::fromDomain)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(itemResponses)
                .status(order.getStatus())
                .deliveryInfo(order.getDeliveryInfo())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
