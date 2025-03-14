package com.sientong.groceries.api.response;

import com.sientong.groceries.domain.cart.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String id;
    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private int quantity;
    private String unit;
    private BigDecimal subtotal;
    private String imageUrl;

    public static CartItemResponse fromDomain(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice().getAmount())
                .currency(item.getCurrency())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .subtotal(item.getSubtotal())
                .imageUrl(item.getImageUrl())
                .build();
    }
}
