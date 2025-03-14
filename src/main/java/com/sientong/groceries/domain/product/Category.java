package com.sientong.groceries.domain.product;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Category {
    String id;
    String name;
    LocalDateTime createdAt;

    public static Category of(String id, String name) {
        return Category.builder()
                .id(id)
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
