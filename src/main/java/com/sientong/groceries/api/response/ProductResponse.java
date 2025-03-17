package com.sientong.groceries.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sientong.groceries.domain.product.Category;
import com.sientong.groceries.domain.product.Product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private int quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse fromDomain(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice().getAmount())
                .category(product.getCategory())
                .quantity(product.getQuantity().getValue())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
