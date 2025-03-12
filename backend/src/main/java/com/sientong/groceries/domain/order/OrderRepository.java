package com.sientong.groceries.domain.order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository {
    Mono<Order> save(Order order);
    Mono<Order> findById(String id);
    Flux<Order> findByUserId(String userId);
    Mono<Order> updateStatus(String id, OrderStatus status);
    Mono<Order> updateDeliveryInfo(String id, DeliveryInfo deliveryInfo);
    Flux<Order> findByStatus(OrderStatus status);
}
