package com.sientong.groceries.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sientong.groceries.api.request.ProductRequest;
import com.sientong.groceries.api.request.StockUpdateRequest;
import com.sientong.groceries.api.response.ProductResponse;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductService;
import com.sientong.groceries.domain.product.Quantity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product management APIs")
public class ProductController {
    private final ProductService productService;

    @Operation(
        summary = "Get all products",
        description = "Retrieve a list of all available products"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Flux<ProductResponse> getAllProducts() {
        return productService.findAll()
                .map(ProductResponse::fromDomain);
    }

    @Operation(
        summary = "Get product by ID",
        description = "Retrieve a specific product by its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public Mono<ProductResponse> getProductById(
        @Parameter(description = "Product ID", required = true)
        @PathVariable String id
    ) {
        return productService.findById(id)
                .map(ProductResponse::fromDomain);
    }

    @Operation(
        summary = "Get products by category",
        description = "Retrieve all products in a specific category"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/category/{categoryId}")
    public Flux<ProductResponse> getProductsByCategory(
        @Parameter(description = "Product category ID", required = true)
        @PathVariable String categoryId
    ) {
        return productService.findByCategory(categoryId)
                .map(ProductResponse::fromDomain);
    }

    @Operation(
        summary = "Create new product",
        description = "Create a new product (SELLER role required)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires SELLER role"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SELLER')")
    public Mono<ProductResponse> createProduct(
        @Parameter(description = "Product to create", required = true)
        @Valid @RequestBody ProductRequest request
    ) {
        Product product = request.toDomain();
        return productService.createProduct(product)
                .map(ProductResponse::fromDomain);
    }

    @Operation(
        summary = "Update product",
        description = "Update an existing product (SELLER role required)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires SELLER role"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public Mono<ProductResponse> updateProduct(
        @Parameter(description = "Product ID", required = true)
        @PathVariable String id,
        @Parameter(description = "Updated product details", required = true)
        @Valid @RequestBody ProductRequest request
    ) {
        Product product = request.toDomain();
        return productService.updateProduct(id, product)
                .map(ProductResponse::fromDomain);
    }

    @Operation(
        summary = "Delete product",
        description = "Delete an existing product (SELLER role required)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires SELLER role"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SELLER')")
    public Mono<Void> deleteProduct(
        @Parameter(description = "Product ID", required = true)
        @PathVariable String id
    ) {
        return productService.deleteProduct(id);
    }

    @Operation(
        summary = "Update product stock",
        description = "Update the stock quantity of a product (SELLER role required)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires SELLER role"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('SELLER')")
    public Mono<ProductResponse> updateStock(
        @Parameter(description = "Product ID", required = true)
        @PathVariable String id,
        @Parameter(description = "Stock update request", required = true)
        @Valid @RequestBody StockUpdateRequest request
    ) {
        return productService.updateStock(id, Quantity.of(request.getQuantity()))
                .map(ProductResponse::fromDomain);
    }
}
