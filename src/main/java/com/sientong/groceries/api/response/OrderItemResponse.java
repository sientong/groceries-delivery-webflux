package com.sientong.groceries.api.response;

import java.math.BigDecimal;

import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.order.OrderItem;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderItemResponse {
    String productId;
    String productName;
    BigDecimal price;
    String currency;
    Quantity quantity;
    BigDecimal subtotal;

    public static OrderItemResponse fromDomain(OrderItem item) {
        return OrderItemResponse.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .price(item.getUnitPrice().getAmount())
                .currency(item.getUnitPrice().getCurrency())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal().getAmount())
                .build();
    }
}
