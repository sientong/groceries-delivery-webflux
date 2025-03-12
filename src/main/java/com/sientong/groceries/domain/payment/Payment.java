package com.sientong.groceries.domain.payment;

import com.sientong.groceries.domain.product.Money;
import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

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
