package com.sientong.groceries.api.request;

import com.sientong.groceries.domain.product.Category;
import com.sientong.groceries.domain.product.Money;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.Quantity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

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
