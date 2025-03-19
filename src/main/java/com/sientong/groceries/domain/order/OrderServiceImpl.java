package com.sientong.groceries.domain.order;

import org.springframework.stereotype.Service;

import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Override
    public Mono<Order> createOrder(Order order) {
        if (order == null) {
            return Mono.error(() -> new IllegalArgumentException("Order cannot be null"));
        }
        return orderRepository.save(order)
                .flatMap(savedOrder -> notificationService.createNotification(
                        savedOrder.getUserId(),
                        "Order Created",
                        "Your order #" + savedOrder.getId() + " has been created and is pending confirmation.",
                        NotificationType.ORDER_CREATED,
                        savedOrder.getId()
                ).thenReturn(savedOrder));
    }

    @Override
    public Mono<Order> updateOrderStatus(String orderId, OrderStatus newStatus) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Order ID cannot be null or empty"));
        }
        if (newStatus == null) {
            return Mono.error(() -> new IllegalArgumentException("New status cannot be null"));
        }
        
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Order not found: " + orderId)))
                .flatMap(order -> {
                    if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
                        return Mono.error(() -> new IllegalStateException(
                                "Cannot update status of " + order.getStatus().toString().toLowerCase() + " order"));
                    }
                    return orderRepository.updateStatus(orderId, newStatus)
                            .flatMap(updatedOrder -> notificationService.createNotification(
                                    updatedOrder.getUserId(),
                                    "Order Status Updated",
                                    "Your order #" + updatedOrder.getId() + " status has been updated to " + newStatus,
                                    NotificationType.ORDER_STATUS_UPDATED,
                                    updatedOrder.getId()
                            ).thenReturn(updatedOrder));
                });
    }

    @Override
    public Mono<Order> getOrderById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Order ID cannot be null or empty"));
        }
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Order not found: " + orderId)));
    }

    @Override
    public Flux<Order> getOrdersByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Flux.error(() -> new IllegalArgumentException("User ID cannot be null or empty"));
        }
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Mono<Order> trackOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Order ID cannot be null or empty"));
        }
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Order not found: " + orderId)))
                .flatMap(order -> {
                    if (order.getDeliveryInfo() == null) {
                        return Mono.error(() -> new IllegalStateException("Order is not yet out for delivery"));
                    }
                    return Mono.just(order);
                });
    }

    @Override
    public Mono<Order> updateDeliveryInfo(String orderId, DeliveryInfo deliveryInfo) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Order ID cannot be null or empty"));
        }
        if (deliveryInfo == null) {
            return Mono.error(() -> new IllegalArgumentException("Delivery info cannot be null"));
        }

        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Order not found: " + orderId)))
                .flatMap(order -> {
                    if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
                        return Mono.error(() -> new IllegalStateException(
                                "Cannot update delivery info of " + order.getStatus().toString().toLowerCase() + " order"));
                    }
                    return orderRepository.updateDeliveryInfo(orderId, deliveryInfo)
                            .flatMap(updatedOrder -> notificationService.createNotification(
                                    updatedOrder.getUserId(),
                                    "Delivery Update",
                                    "Delivery information for your order #" + updatedOrder.getId() + " has been updated.",
                                    NotificationType.DELIVERY_UPDATE,
                                    updatedOrder.getId()
                            ).thenReturn(updatedOrder));
                });
    }

    @Override
    public Flux<Order> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            return Flux.error(() -> new IllegalArgumentException("Status cannot be null"));
        }
        return orderRepository.findByStatus(status);
    }

    @Override
    public Mono<Order> cancelOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Order ID cannot be null or empty"));
        }

        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Order not found: " + orderId)))
                .flatMap(order -> {
                    if (order.getStatus() == OrderStatus.DELIVERED) {
                        return Mono.error(() -> new IllegalStateException("Cannot cancel a delivered order"));
                    }
                    if (order.getStatus() == OrderStatus.CANCELLED) {
                        return Mono.error(() -> new IllegalStateException("Order is already cancelled"));
                    }
                    return orderRepository.updateStatus(orderId, OrderStatus.CANCELLED)
                            .flatMap(cancelledOrder -> notificationService.createNotification(
                                    cancelledOrder.getUserId(),
                                    "Order Cancelled",
                                    "Your order #" + cancelledOrder.getId() + " has been cancelled.",
                                    NotificationType.ORDER_CANCELLED,
                                    cancelledOrder.getId()
                            ).thenReturn(cancelledOrder));
                });
    }

    @Override
    public Mono<Order> assignSeller(String orderId, String sellerId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Order ID cannot be null or empty"));
        }
        if (sellerId == null || sellerId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Seller ID cannot be null or empty"));
        }

        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Order not found: " + orderId)))
                .flatMap(order -> {
                    if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
                        return Mono.error(() -> new IllegalStateException(
                                "Cannot assign seller to " + order.getStatus().toString().toLowerCase() + " order"));
                    }
                    return orderRepository.assignSeller(orderId, sellerId)
                            .flatMap(updatedOrder -> notificationService.createNotification(
                                    updatedOrder.getUserId(),
                                    "Seller Assigned",
                                    "A seller has been assigned to your order #" + updatedOrder.getId(),
                                    NotificationType.ORDER_SELLER_ASSIGNED,
                                    updatedOrder.getId()
                            ).thenReturn(updatedOrder));
                });
    }

    @Override
    public Flux<Order> getOrders() {
        return orderRepository.findAll();
    }
}
