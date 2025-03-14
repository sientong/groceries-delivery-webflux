package com.sientong.groceries.infrastructure.persistence.entity;

import java.math.BigDecimal;

import org.springframework.data.relational.core.mapping.Table;

import com.sientong.groceries.domain.cart.CartItem;
import com.sientong.groceries.domain.product.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cart_items")
public class CartItemEntity {
    private String id;
    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer quantity;
    private String unit;

    public CartItem toDomain() {
        return CartItem.builder()
                .id(id)
                .productId(productId)
                .name(name)
                .description(description)
                .price(Money.of(price, currency))
                .quantity(quantity)
                .unit(unit)
                .build();
    }

    public static CartItemEntity fromDomain(CartItem item) {
        return CartItemEntity.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice().getAmount())
                .currency(item.getPrice().getCurrency())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .build();
    }
}
