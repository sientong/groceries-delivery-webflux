package com.sientong.groceries.domain.notification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Notification> createNotification(String userId, String title, String message, NotificationType type, String referenceId);
    Flux<Notification> getUserNotifications(String userId);
    Mono<Notification> markNotificationAsRead(String notificationId);
    Mono<Long> getUnreadNotificationCount(String userId);
    Flux<Notification> getUserNotificationStream(String userId);
}
