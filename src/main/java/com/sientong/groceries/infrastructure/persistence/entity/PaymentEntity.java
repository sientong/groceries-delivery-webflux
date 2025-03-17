package com.sientong.groceries.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.payment.Payment;
import com.sientong.groceries.domain.payment.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("payments")
public class PaymentEntity {
    @Id
    private String id;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment toDomain() {
        return Payment.builder()
                .id(id)
                .orderId(orderId)
                .userId(userId)
                .amount(Money.of(amount))
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static PaymentEntity fromDomain(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount().getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
