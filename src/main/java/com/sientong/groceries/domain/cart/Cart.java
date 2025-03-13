package com.sientong.groceries.domain.cart;

import java.time.LocalDateTime;
import java.util.List;

import com.sientong.groceries.domain.product.Money;

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
    private List<CartItem> items;
    private Money total;
    private String couponCode;
    private Money discount;
    private LocalDateTime updatedAt;

    public void addItem(CartItem item) {
        items.add(item);
        recalculateTotal();
    }

    public void removeItem(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
        recalculateTotal();
    }

    public void updateItemQuantity(String itemId, int quantity) {
        items.stream()
            .filter(item -> item.getId().equals(itemId))
            .findFirst()
            .ifPresent(item -> {
                item.setQuantity(quantity);
                recalculateTotal();
            });
    }

    public void clear() {
        items.clear();
        couponCode = null;
        discount = Money.ZERO;
        recalculateTotal();
    }

    private void recalculateTotal() {
        total = items.stream()
            .<Money>map(item -> item.getPrice().multiply(item.getQuantity()))
            .reduce(Money.ZERO, Money::add);

        if (discount != null) {
            total = total.subtract(discount);
        }

        updatedAt = LocalDateTime.now();
    }
}
