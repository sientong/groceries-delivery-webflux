package com.sientong.groceries.api.controller;

import com.sientong.groceries.api.response.CategoryResponse;
import com.sientong.groceries.domain.category.Category;
import com.sientong.groceries.domain.category.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {
    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves all available product categories")
    public Flux<CategoryResponse> getCategories() {
        return categoryRepository.findAll()
                .map(CategoryResponse::fromDomain);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its ID")
    public Mono<ResponseEntity<CategoryResponse>> getCategoryById(@PathVariable String id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::fromDomain)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(
        summary = "Create new category", 
        description = "Creates a new product category. Requires ADMIN or SELLER role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public Mono<CategoryResponse> createCategory(@RequestBody Category category) {
        return categoryRepository.save(category)
                .map(CategoryResponse::fromDomain);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete category", 
        description = "Deletes an existing category. Requires ADMIN role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public Mono<ResponseEntity<Void>> deleteCategory(@PathVariable String id) {
        return categoryRepository.deleteById(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
