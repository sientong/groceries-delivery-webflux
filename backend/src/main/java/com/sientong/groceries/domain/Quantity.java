package com.sientong.groceries.domain;

import lombok.Value;

@Value(staticConstructor = "of")
public class Quantity {
    int value;

    private Quantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.value = value;
    }
}
