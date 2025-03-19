package com.sientong.groceries.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @Positive(message = "Quantity must be positive")
    private int quantity;
}
