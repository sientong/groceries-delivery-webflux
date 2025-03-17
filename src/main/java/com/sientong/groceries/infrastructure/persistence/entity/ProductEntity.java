package com.sientong.groceries.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.product.Category;
import com.sientong.groceries.domain.product.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class ProductEntity {
    @Id
    private String id;
    private String name;
    private String description;
    @Column("category_id")
    private String categoryId;
    @Column("category_name")
    private String categoryName;
    private BigDecimal price;
    private String currency;
    private int quantity;
    private String unit;
    @Column("image_url")
    private String imageUrl;
    @Column("created_at")
    private LocalDateTime createdAt;
    @Column("updated_at")
    private LocalDateTime updatedAt;

    public Product toDomain() {
        return new Product(
                id,
                name,
                description,
                Money.of(price, currency),
                Category.of(categoryId, categoryName),
                Quantity.of(quantity, unit),
                imageUrl,
                createdAt,
                updatedAt
        );
    }

    public static ProductEntity fromDomain(Product product) {
        return ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .price(product.getPrice().getAmount())
                .currency(product.getPrice().getCurrency())
                .quantity(product.getQuantity().getValue())
                .unit(product.getQuantity().getUnit())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
