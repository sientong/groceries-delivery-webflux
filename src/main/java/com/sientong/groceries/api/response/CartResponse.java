package com.sientong.groceries.api.response;

import com.sientong.groceries.domain.cart.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String id;
    private String userId;
    private List<CartItemResponse> items;
    private BigDecimal total;
    private String currency;
    private LocalDateTime updatedAt;

    public static CartResponse fromDomain(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems().stream()
                        .map(CartItemResponse::fromDomain)
                        .toList())
                .total(cart.getTotal().getAmount())
                .currency(cart.getTotal().getCurrency())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
