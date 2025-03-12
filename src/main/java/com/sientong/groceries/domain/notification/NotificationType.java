package com.sientong.groceries.domain.notification;

public enum NotificationType {
    // Order notifications
    ORDER_CREATED,
    ORDER_STATUS_UPDATED,
    ORDER_CANCELLED,
    DELIVERY_UPDATE,
    
    // Payment notifications
    PAYMENT_RECEIVED,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED,
    
    // Inventory notifications
    STOCK_ALERT,
    PRODUCT_ADDED,
    PRODUCT_UPDATED,
    
    // User notifications
    USER_REGISTERED,
    USER_VERIFIED,
    PASSWORD_RESET,
    
    // Marketing notifications
    PROMOTION,
    SPECIAL_OFFER,
    SEASONAL_DEALS
}
