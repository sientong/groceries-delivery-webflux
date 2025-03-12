package com.sientong.groceries.infrastructure.persistence.repository;

import com.sientong.groceries.domain.notification.NotificationType;
import com.sientong.groceries.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveNotificationRepository extends ReactiveCrudRepository<NotificationEntity, String> {
    Flux<NotificationEntity> findByUserId(String userId);
    
    Flux<NotificationEntity> findByUserIdAndIsReadFalse(String userId);
    
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND type = :type ORDER BY created_at DESC")
    Flux<NotificationEntity> findByUserIdAndType(String userId, NotificationType type);
    
    @Modifying
    @Query("UPDATE notifications SET is_read = true WHERE id = :id")
    Mono<Boolean> markAsRead(String id);
    
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = false")
    Mono<Long> countUnreadByUserId(String userId);
    
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    Flux<NotificationEntity> findRecentByUserId(String userId, int limit);
}
