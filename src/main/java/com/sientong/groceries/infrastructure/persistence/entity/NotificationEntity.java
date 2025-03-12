package com.sientong.groceries.infrastructure.persistence.entity;

import com.sientong.groceries.domain.notification.Notification;
import com.sientong.groceries.domain.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("notifications")
public class NotificationEntity {
    @Id
    private String id;
    private String userId;
    private String title;
    private String message;
    private NotificationType type;
    private String referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public Notification toDomain() {
        return Notification.builder()
                .id(id)
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .read(isRead)
                .createdAt(createdAt)
                .build();
    }

    public static NotificationEntity fromDomain(Notification notification) {
        return NotificationEntity.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .referenceId(notification.getReferenceId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
