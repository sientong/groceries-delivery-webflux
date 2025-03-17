package com.sientong.groceries.api.request;

import java.math.BigDecimal;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.product.Category;
import com.sientong.groceries.domain.product.Product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private int quantity;

    private String unit;

    public Product toDomain() {
        return Product.builder()
                .name(name)
                .description(description)
                .price(Money.of(price))
                .category(category)
                .quantity(Quantity.of(quantity, unit != null ? unit : "piece"))
                .build();
    }
}
