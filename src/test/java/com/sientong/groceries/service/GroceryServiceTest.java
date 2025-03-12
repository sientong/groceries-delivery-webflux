package com.sientong.groceries.service;

import com.sientong.groceries.model.Grocery;
import com.sientong.groceries.repository.GroceryRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;

class GroceryServiceTest {

    @Test
    void getAllGroceries_ShouldReturnList() {
        GroceryRepository mockRepo = mock(GroceryRepository.class);
        when(mockRepo.findAll()).thenReturn(Flux.just(
            new Grocery("1", "Apple", 3.0),
            new Grocery("2", "Banana", 2.0)
        ));

        GroceryService service = new GroceryService(mockRepo);

        StepVerifier.create(service.getAllGroceries())
            .expectNextMatches(g -> g.getName().equals("Apple"))
            .expectNextMatches(g -> g.getName().equals("Banana"))
            .verifyComplete();
    }
}