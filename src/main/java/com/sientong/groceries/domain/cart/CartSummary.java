package com.sientong.groceries.domain.cart;

import com.sientong.groceries.domain.product.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummary {
    private int totalItems;
    private Money total;
}
