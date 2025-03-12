package com.sientong.groceries.domain.notification;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class Notification {
    private final String id;
    private final String userId;
    private final String title;
    private final String message;
    private final NotificationType type;
    private final String referenceId;
    private boolean read;
    private final LocalDateTime createdAt;

    public Notification(String id, String userId, String title, String message, 
                       NotificationType type, String referenceId, boolean read, 
                       LocalDateTime createdAt) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }

        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.read = read;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void markAsRead() {
        this.read = true;
    }
}
