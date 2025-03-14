package com.sientong.groceries.infrastructure.persistence.repository;

import com.sientong.groceries.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactiveCategoryRepository extends R2dbcRepository<CategoryEntity, String> {
}
