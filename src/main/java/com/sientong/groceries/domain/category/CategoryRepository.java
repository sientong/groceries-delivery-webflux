package com.sientong.groceries.domain.category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryRepository {
    Flux<Category> findAll();
    Mono<Category> findById(String id);
    Mono<Category> save(Category category);
    Mono<Void> deleteById(String id);
}
