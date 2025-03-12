package com.sientong.groceries.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final Many<Notification> notificationSink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public Mono<Notification> createNotification(String userId, String title, String message, 
            NotificationType type, String referenceId) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification)
                .doOnSuccess(saved -> notificationSink.tryEmitNext(saved));
    }

    @Override
    public Flux<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public Mono<Notification> markNotificationAsRead(String notificationId) {
        return notificationRepository.markAsRead(notificationId);
    }

    @Override
    public Mono<Long> getUnreadNotificationCount(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    public Flux<Notification> getUserNotificationStream(String userId) {
        return Flux.concat(
            getUserNotifications(userId),
            notificationSink.asFlux()
                .filter(notification -> notification.getUserId().equals(userId))
        );
    }
}
