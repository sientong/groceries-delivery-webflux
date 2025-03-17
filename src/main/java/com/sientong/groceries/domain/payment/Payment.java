package com.sientong.groceries.domain.payment;

import java.time.LocalDateTime;

import com.sientong.groceries.domain.common.Money;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Payment {
    String id;
    String orderId;
    String userId;
    Money amount;
    PaymentStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
