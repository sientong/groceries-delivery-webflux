package com.sientong.groceries.domain.order;

import lombok.Value;
import java.time.LocalDateTime;

@Value(staticConstructor = "of")
public class DeliveryInfo {
    String address;
    String phone;
    String trackingNumber;
    LocalDateTime estimatedDeliveryTime;
    String deliveryNotes;

    private DeliveryInfo(String address, String phone, String trackingNumber, LocalDateTime estimatedDeliveryTime, String deliveryNotes) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address cannot be null or empty");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        
        this.address = address;
        this.phone = phone;
        this.trackingNumber = trackingNumber;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.deliveryNotes = deliveryNotes;
    }
}
