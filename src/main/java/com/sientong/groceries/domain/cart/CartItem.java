package com.sientong.groceries.domain.cart;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sientong.groceries.domain.common.Money;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItem {
    private String id;
    private String cartId;
    private String productId;
    private String name;
    private String description;
    private Money price;
    private Integer quantity;
    private String unit;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItem createNew() {
        return CartItem.builder()
                .id(UUID.randomUUID().toString())
                .cartId("")
                .productId("")
                .name("")
                .description("")
                .price(Money.ZERO)
                .quantity(0)
                .unit("pcs")
                .imageUrl("")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Money getSubtotal() {
        return price.multiply(quantity);
    }

    public void update(CartItem other) {
        if (other.getName() != null) {
            this.name = other.getName();
        }
        if (other.getDescription() != null) {
            this.description = other.getDescription();
        }
        if (other.getPrice() != null) {
            this.price = other.getPrice();
        }
        if (other.getQuantity() != null) {
            this.quantity = other.getQuantity();
        }
        if (other.getUnit() != null) {
            this.unit = other.getUnit();
        }
        if (other.getImageUrl() != null) {
            this.imageUrl = other.getImageUrl();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementQuantity(int amount) {
        this.quantity += amount;
    }

    public void decrementQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public Money getPrice() {
        return price != null ? price : Money.ZERO;
    }

    public String getUnit() {
        return unit != null ? unit : "pcs";
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public String getCurrency() {
        return getPrice().getCurrency();
    }
}
