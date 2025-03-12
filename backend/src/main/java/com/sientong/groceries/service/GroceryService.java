package com.sientong.groceries.service;

import org.springframework.stereotype.Service;

import com.sientong.groceries.model.Grocery;
import com.sientong.groceries.repository.GroceryRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GroceryService {

    private final GroceryRepository repository;

    public GroceryService(GroceryRepository repository) {
        this.repository = repository;
    }

    public Flux<Grocery> getAllGroceries() {
        return repository.findAll();
    }

    public Mono<Grocery> createGrocery(Grocery grocery) {
        return repository.save(grocery);
    }
}