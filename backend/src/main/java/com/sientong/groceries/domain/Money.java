package com.sientong.groceries.domain;

import lombok.Value;
import java.math.BigDecimal;

@Value(staticConstructor = "of")
public class Money {
    BigDecimal amount;

    private Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        this.amount = amount;
    }
}
