package com.sientong.groceries.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sientong.groceries.domain.cart.CartItem;
import com.sientong.groceries.domain.common.Money;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private String id;
    private String productId;
    private String name;
    private String description;
    private MoneyResponse price;
    private MoneyResponse subtotal;
    private Integer quantity;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;

    public static CartItemResponse fromDomain(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .name(item.getName())
                .description(item.getDescription())
                .price(MoneyResponse.fromDomain(item.getPrice()))
                .subtotal(MoneyResponse.fromDomain(item.getSubtotal()))
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .imageUrl(item.getImageUrl())
                .build();
    }

    @Data
    @Builder
    public static class MoneyResponse {
        private BigDecimal amount;
        private String currency;

        public static MoneyResponse fromDomain(Money money) {
            return MoneyResponse.builder()
                    .amount(money.getAmount())
                    .currency(money.getCurrency())
                    .build();
        }
    }
}
