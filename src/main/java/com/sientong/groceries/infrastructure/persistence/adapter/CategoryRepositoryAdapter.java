package com.sientong.groceries.infrastructure.persistence.adapter;

import com.sientong.groceries.domain.category.Category;
import com.sientong.groceries.domain.category.CategoryRepository;
import com.sientong.groceries.infrastructure.persistence.entity.CategoryEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {
    private final ReactiveCategoryRepository repository;

    @Override
    public Flux<Category> findAll() {
        return repository.findAll()
                .map(this::toCategory);
    }

    @Override
    public Mono<Category> findById(String id) {
        return repository.findById(id)
                .map(this::toCategory);
    }

    @Override
    public Mono<Category> save(Category category) {
        CategoryEntity entity = toEntity(category);
        return repository.save(entity)
                .map(this::toCategory);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }

    private Category toCategory(CategoryEntity entity) {
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    private CategoryEntity toEntity(Category category) {
        return CategoryEntity.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
