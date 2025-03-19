package com.sientong.groceries.domain.order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
    Mono<Order> createOrder(Order order);
    
    Mono<Order> updateOrderStatus(String orderId, OrderStatus newStatus);
    
    Mono<Order> getOrderById(String orderId);
    
    Flux<Order> getOrdersByUserId(String userId);
    
    Mono<Order> trackOrder(String orderId);
    
    Mono<Order> updateDeliveryInfo(String orderId, DeliveryInfo deliveryInfo);
    
    Flux<Order> getOrdersByStatus(OrderStatus status);
    
    Mono<Order> cancelOrder(String orderId);
    
    Mono<Order> assignSeller(String orderId, String sellerId);
    
    Flux<Order> getOrders();
}
