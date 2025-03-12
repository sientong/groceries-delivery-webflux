package com.sientong.groceries.controller;

import com.sientong.groceries.domain.Product;
import com.sientong.groceries.domain.ProductCategory;
import com.sientong.groceries.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class GroceryController {

    private final ProductService productService;

    @GetMapping
    public Flux<Product> getAllProducts(@RequestParam(required = false) ProductCategory category) {
        if (category != null) {
            return productService.getProductsByCategory(category);
        }
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Mono<Product> getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @PatchMapping("/{id}/stock")
    public Mono<Product> updateStock(@PathVariable String id, @RequestBody StockUpdateRequest request) {
        return productService.updateStock(id, request.getQuantityDelta());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteProduct(@PathVariable String id) {
        return productService.deleteProduct(id);
    }
}