package com.sientong.groceries.service;

import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderRepository;
import com.sientong.groceries.domain.order.OrderStatus;
import com.sientong.groceries.domain.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public Mono<Order> createOrder(Order order) {
        return orderRepository.save(order)
                .flatMap(savedOrder -> notificationService
                        .sendOrderNotification(savedOrder, OrderStatus.PENDING)
                        .thenReturn(savedOrder));
    }

    public Mono<Order> updateOrderStatus(String orderId, OrderStatus newStatus) {
        return orderRepository.updateStatus(orderId, newStatus)
                .flatMap(updatedOrder -> notificationService
                        .sendOrderNotification(updatedOrder, newStatus)
                        .thenReturn(updatedOrder));
    }

    public Mono<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }

    public Flux<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public Mono<Order> trackOrder(String orderId) {
        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    if (order.getDeliveryInfo() == null) {
                        return Mono.error(new IllegalStateException("Order is not yet out for delivery"));
                    }
                    return Mono.just(order);
                });
    }

    public Mono<Order> updateDeliveryInfo(String orderId, Order.DeliveryInfo deliveryInfo) {
        return orderRepository.updateDeliveryInfo(orderId, deliveryInfo)
                .flatMap(updatedOrder -> notificationService
                        .sendDeliveryUpdate(updatedOrder)
                        .thenReturn(updatedOrder));
    }

    public Flux<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Mono<Order> cancelOrder(String orderId) {
        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    if (order.getStatus() == OrderStatus.DELIVERED) {
                        return Mono.error(new IllegalStateException("Cannot cancel a delivered order"));
                    }
                    return orderRepository.updateStatus(orderId, OrderStatus.CANCELLED)
                            .flatMap(cancelledOrder -> notificationService
                                    .sendOrderNotification(cancelledOrder, OrderStatus.CANCELLED)
                                    .thenReturn(cancelledOrder));
                });
    }
}
