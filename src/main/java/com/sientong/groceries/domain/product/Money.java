package com.sientong.groceries.domain.product;

import lombok.Value;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Value(staticConstructor = "of")
public class Money {
    public static final Money ZERO = Money.of(BigDecimal.ZERO);
    
    BigDecimal amount;

    private Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        this.amount = amount;
    }

    public Money multiply(int quantity) {
        return Money.of(this.amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot add null Money");
        }
        return Money.of(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot subtract null Money");
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Result cannot be negative");
        }
        return Money.of(result);
    }

    public String formatWithCurrency() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        return formatter.format(amount);
    }
}
