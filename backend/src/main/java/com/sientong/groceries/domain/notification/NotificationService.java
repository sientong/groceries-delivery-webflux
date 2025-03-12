package com.sientong.groceries.domain.notification;

import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> sendOrderNotification(Order order, OrderStatus newStatus);
    Mono<Void> sendDeliveryUpdate(Order order);
    Flux<Notification> getUserNotifications(String userId);
    Mono<Notification> markNotificationAsRead(String notificationId);
    Mono<Long> getUnreadNotificationCount(String userId);
}
