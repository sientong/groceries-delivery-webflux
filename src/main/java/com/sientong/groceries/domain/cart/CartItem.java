package com.sientong.groceries.domain.cart;

import com.sientong.groceries.domain.product.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String id;
    private String productId;
    private String name;
    private String description;
    private Money price;
    private int quantity;
    private String unit;
    private String imageUrl;

    public BigDecimal getSubtotal() {
        return price.getAmount().multiply(BigDecimal.valueOf(quantity));
    }

    public String getCurrency() {
        return price.getCurrency();
    }
}
