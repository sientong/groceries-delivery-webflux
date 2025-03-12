package com.sientong.groceries.api.request;

import com.sientong.groceries.domain.product.Quantity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating product stock quantity")
public class StockUpdateRequest {
    @Schema(
        description = "Change in quantity (positive for increase, negative for decrease)",
        example = "10",
        required = true
    )
    @NotNull(message = "Quantity delta cannot be null")
    private Quantity quantityDelta;
}
