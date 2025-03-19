package com.sientong.groceries.domain.notification;

import java.time.LocalDateTime;

public class MockableNotification extends Notification {
    public MockableNotification() {
        super("test-id", "test-user", "Test Title", "Test Message", 
              NotificationType.ORDER_CREATED, "test-ref", false, LocalDateTime.now());
    }
}
