package com.sientong.groceries.domain.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Value;

@Value
public class Quantity {
    @Positive(message = "Quantity must be positive")
    int value;

    @NotBlank(message = "Unit cannot be empty")
    String unit;

    private Quantity(int value, String unit) {
        if (value <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be null or empty");
        }
        this.value = value;
        this.unit = unit;
    }

    public static Quantity of(int value) {
        return new Quantity(value, "piece");
    }

    public static Quantity of(int value, String unit) {
        return new Quantity(value, unit);
    }

    @Override
    public String toString() {
        return String.format("%d %s%s", value, unit, value != 1 ? "s" : "");
    }
}
