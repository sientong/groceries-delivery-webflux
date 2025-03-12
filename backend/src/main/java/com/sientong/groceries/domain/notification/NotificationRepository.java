package com.sientong.groceries.domain.notification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationRepository {
    Mono<Notification> save(Notification notification);
    Flux<Notification> findByUserId(String userId);
    Mono<Notification> markAsRead(String id);
    Flux<Notification> findUnreadByUserId(String userId);
    Mono<Long> countUnreadByUserId(String userId);
}
