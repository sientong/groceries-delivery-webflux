package com.sientong.groceries.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import com.sientong.groceries.domain.cart.Cart;
import com.sientong.groceries.domain.common.Money;

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
    @Transient
    @Builder.Default
    private List<CartItemEntity> items = new ArrayList<>();
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
                .total(Money.of(total != null ? total : BigDecimal.ZERO, 
                        currency != null ? currency : Money.DEFAULT_CURRENCY))
                .updatedAt(updatedAt != null ? updatedAt : LocalDateTime.now())
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
