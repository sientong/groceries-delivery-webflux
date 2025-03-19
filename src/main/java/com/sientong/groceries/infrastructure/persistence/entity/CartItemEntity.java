package com.sientong.groceries.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.sientong.groceries.domain.cart.CartItem;
import com.sientong.groceries.domain.common.Money;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Table("cart_items")
public class CartItemEntity {
    @Id
    private String id;
    private String cartId;
    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private int quantity;
    private String unit;
    private String imageUrl;
    private LocalDateTime updatedAt;

    public CartItem toDomain() {
        return CartItem.builder()
                .id(id)
                .productId(productId)
                .name(name != null ? name : "")
                .description(description != null ? description : "")
                .price(Money.of(price, currency))
                .quantity(quantity)
                .unit(unit != null ? unit : "pcs")
                .imageUrl(imageUrl != null ? imageUrl : "")
                .build();
    }

    public static CartItemEntity fromDomain(CartItem item) {
        return CartItemEntity.builder()
                .id(item.getId())
                .cartId(item.getCartId())
                .productId(item.getProductId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice() != null ? item.getPrice().getAmount() : Money.ZERO.getAmount())
                .currency(item.getPrice() != null ? item.getPrice().getCurrency() : Money.ZERO.getCurrency())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .imageUrl(item.getImageUrl())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
