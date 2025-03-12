package com.sientong.groceries.api.request;

import com.sientong.groceries.domain.order.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new order")
public class CreateOrderRequest {
    @Schema(description = "List of items to order", required = true)
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItem> items;
}
