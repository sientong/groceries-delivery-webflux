package com.sientong.groceries.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.sientong.groceries.domain.cart.Cart;
import com.sientong.groceries.domain.product.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("carts")
public class CartEntity {
    @Id
    private String id;
    private String userId;
    private List<CartItemEntity> items;
    private BigDecimal total;
    private String currency;
    private LocalDateTime updatedAt;

    public Cart toDomain() {
        return Cart.builder()
                .id(id)
                .userId(userId)
                .items(items.stream()
                        .map(CartItemEntity::toDomain)
                        .toList())
                .total(Money.of(total, currency))
                .updatedAt(updatedAt)
                .build();
    }

    public static CartEntity fromDomain(Cart cart) {
        return CartEntity.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems().stream()
                        .map(CartItemEntity::fromDomain)
                        .toList())
                .total(cart.getTotal().getAmount())
                .currency(cart.getTotal().getCurrency())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
