package com.sientong.groceries.api.response;

import java.time.LocalDateTime;

import com.sientong.groceries.domain.notification.Notification;
import com.sientong.groceries.domain.notification.NotificationType;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationResponse {
    String id;
    String recipientId;
    String title;
    String message;
    NotificationType type;
    boolean read;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static NotificationResponse fromDomain(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
