package com.sientong.groceries.domain.order;

import com.sientong.groceries.domain.product.Money;
import com.sientong.groceries.domain.product.Quantity;
import lombok.Getter;

@Getter
public class OrderItem {
    private final String productId;
    private final String productName;
    private final Money unitPrice;
    private final Quantity quantity;
    private final Money subtotal;

    public OrderItem(String productId, String productName, Money unitPrice, Quantity quantity) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }

        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = Money.of(unitPrice.getAmount().multiply(java.math.BigDecimal.valueOf(quantity.getValue())));
    }

    public static OrderItem of(String productId, String productName, Money unitPrice, Quantity quantity) {
        return new OrderItem(productId, productName, unitPrice, quantity);
    }
}
