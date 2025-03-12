package com.sientong.groceries.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.sientong.groceries.model.Grocery;

public interface GroceryRepository extends ReactiveCrudRepository<Grocery, String> {
}