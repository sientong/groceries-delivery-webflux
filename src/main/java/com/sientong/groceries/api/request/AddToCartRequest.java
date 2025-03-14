package com.sientong.groceries.api.request;

import com.sientong.groceries.domain.cart.CartItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    public CartItem toDomain() {
        return CartItem.builder()
                .productId(productId)
                .quantity(quantity)
                .unit(unit)
                .build();
    }
}
