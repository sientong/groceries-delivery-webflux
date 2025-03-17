package com.sientong.groceries.domain.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Money total;
    private LocalDateTime updatedAt;

    public void addItem(CartItem item) {
        items.add(item);
        recalculateTotal();
    }

    public void updateItem(String itemId, CartItem updatedItem) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(itemId)) {
                CartItem existingItem = items.get(i);
                existingItem.setQuantity(updatedItem.getQuantity());
                break;
            }
        }
        recalculateTotal();
    }

    public void removeItem(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
        recalculateTotal();
    }

    public void clear() {
        items.clear();
        recalculateTotal();
    }

    private void recalculateTotal() {
        BigDecimal subtotal = getSubtotal();
        total = Money.of(subtotal, items.isEmpty() ? "USD" : items.get(0).getCurrency());
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
