package com.sientong.groceries.domain.cart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sientong.groceries.domain.common.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private String id;
    private String userId;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    @Builder.Default
    private Money total = Money.ZERO;
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void addItem(CartItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }

        Optional<CartItem> existingItem = items.stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem existing = existingItem.get();
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            items.add(item);
        }

        recalculateTotal();
    }

    public void updateItem(String itemId, CartItem updatedItem) {
        if (items == null) {
            items = new ArrayList<>();
            return;
        }

        items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(updatedItem.getQuantity());
                    if (updatedItem.getUnit() != null) {
                        item.setUnit(updatedItem.getUnit());
                    }
                });

        recalculateTotal();
    }

    public void removeItem(String itemId) {
        if (items == null) {
            items = new ArrayList<>();
            return;
        }

        items.removeIf(item -> item.getId().equals(itemId));
        recalculateTotal();
    }

    public Cart clear() {
        if (items == null) {
            items = new ArrayList<>();
        } else {
            items.clear();
        }
        total = Money.ZERO;
        updatedAt = LocalDateTime.now();
        return this;
    }

    private void recalculateTotal() {
        if (items == null || items.isEmpty()) {
            total = Money.ZERO;
            updatedAt = LocalDateTime.now();
            return;
        }

        total = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(Money.ZERO, Money::add);
        updatedAt = LocalDateTime.now();
    }

    public Money getSubtotal() {
        if (getItems().isEmpty()) {
            return Money.ZERO;
        }

        return getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(Money.ZERO, Money::add);
    }

    public List<CartItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }
}
