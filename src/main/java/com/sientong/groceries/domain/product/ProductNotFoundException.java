package com.sientong.groceries.domain.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ProductNotFoundException extends ResponseStatusException {
    public ProductNotFoundException(String productId) {
        super(HttpStatus.NOT_FOUND, String.format("Product with ID '%s' not found", productId));
    }
}
