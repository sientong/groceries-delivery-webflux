package com.sientong.groceries.api.response;

import com.sientong.groceries.domain.cart.CartSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryResponse {
    private int totalItems;
    private BigDecimal total;
    private String currency;

    public static CartSummaryResponse fromDomain(CartSummary summary) {
        return CartSummaryResponse.builder()
                .totalItems(summary.getTotalItems())
                .total(summary.getTotal().getAmount())
                .currency(summary.getTotal().getCurrency())
                .build();
    }
}
