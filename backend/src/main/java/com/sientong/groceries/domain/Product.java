package com.sientong.groceries.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Product {
    private final String id;
    private final String name;
    private final String description;
    private final Money price;
    private final ProductCategory category;
    private Quantity quantity;

    public Product(String id, String name, String description, Money price, ProductCategory category, Quantity quantity) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.quantity = quantity != null ? quantity : Quantity.of(0);
    }

    public void updateStock(int delta) {
        int newQuantity = this.quantity.getValue() + delta;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Cannot reduce stock below zero");
        }
        this.quantity = Quantity.of(newQuantity);
    }
}
