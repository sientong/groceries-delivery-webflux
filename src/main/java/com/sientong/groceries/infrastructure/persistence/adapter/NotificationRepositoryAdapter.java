package com.sientong.groceries.infrastructure.persistence.adapter;

import com.sientong.groceries.domain.notification.Notification;
import com.sientong.groceries.domain.notification.NotificationRepository;
import com.sientong.groceries.infrastructure.persistence.entity.NotificationEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {
    private final ReactiveNotificationRepository notificationRepository;

    @Override
    public Mono<Notification> save(Notification notification) {
        return notificationRepository.save(NotificationEntity.fromDomain(notification))
                .map(NotificationEntity::toDomain);
    }

    @Override
    public Flux<Notification> findByUserId(String userId) {
        return notificationRepository.findByUserId(userId)
                .map(NotificationEntity::toDomain);
    }

    @Override
    public Mono<Notification> markAsRead(String id) {
        return notificationRepository.markAsRead(id)
                .filter(updated -> updated)
                .flatMap(updated -> notificationRepository.findById(id))
                .map(NotificationEntity::toDomain);
    }

    @Override
    public Flux<Notification> findUnreadByUserId(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId)
                .map(NotificationEntity::toDomain);
    }

    @Override
    public Mono<Long> countUnreadByUserId(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
}
