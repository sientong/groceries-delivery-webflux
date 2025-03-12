package com.sientong.groceries.domain.product;

import lombok.Value;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Value(staticConstructor = "of")
public class Money {
    BigDecimal amount;

    private Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        this.amount = amount;
    }

    public String formatWithCurrency() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        return formatter.format(amount);
    }
}
