package com.sientong.groceries.infrastructure.persistence.entity;

import com.sientong.groceries.domain.product.Money;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductCategory;
import com.sientong.groceries.domain.product.Quantity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private BigDecimal price;
    private ProductCategory category;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product toDomain() {
        return new Product(
            id,
            name,
            description,
            Money.of(price),
            category,
            Quantity.of(quantity)
        );
    }

    public static ProductEntity fromDomain(Product product) {
        return ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice().getAmount())
                .category(product.getCategory())
                .quantity(product.getQuantity().getValue())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
