package com.sientong.groceries.api.request;

import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderRequest {
    @NotNull(message = "User ID cannot be null")
    private String userId;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> items;

    public Order toDomain() {
        List<OrderItem> orderItems = items.stream()
                .map(OrderItemRequest::toDomain)
                .collect(Collectors.toList());

        return Order.builder()
                .userId(userId)
                .items(orderItems)
                .build();
    }
}
