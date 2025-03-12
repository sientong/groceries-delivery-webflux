package com.sientong.groceries.domain.payment;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;
import com.sientong.groceries.domain.order.Order;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    public Mono<Boolean> processPayment(Order order) {
        if (order == null) {
            return Mono.error(() -> new IllegalArgumentException("Order cannot be null"));
        }
        if (order.getTotal() == null || order.getTotal().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(() -> new IllegalArgumentException("Invalid order total"));
        }

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .amount(order.getTotal())
                .status(PaymentStatus.PENDING)
                .build();

        return paymentRepository.save(payment)
                .flatMap(savedPayment -> paymentGateway.processPayment(
                        savedPayment.getId(),
                        savedPayment.getAmount())
                        .flatMap(paymentResult -> {
                            PaymentStatus status = paymentResult ? 
                                    PaymentStatus.COMPLETED : 
                                    PaymentStatus.FAILED;
                            
                            return paymentRepository.updateStatus(payment.getId(), status)
                                    .flatMap(updatedPayment -> {
                                        String title = paymentResult ? "Payment Successful" : "Payment Failed";
                                        String message = paymentResult ?
                                                String.format("Payment of %s for order #%s has been processed successfully.", 
                                                        payment.getAmount().formatWithCurrency(),
                                                        order.getId()) :
                                                String.format("Payment of %s for order #%s has failed. Please try again.", 
                                                        payment.getAmount().formatWithCurrency(),
                                                        order.getId());
                                        NotificationType type = paymentResult ? 
                                                NotificationType.PAYMENT_RECEIVED :
                                                NotificationType.PAYMENT_FAILED;

                                        return notificationService.createNotification(
                                                order.getUserId(),
                                                title,
                                                message,
                                                type,
                                                order.getId()
                                        ).thenReturn(paymentResult);
                                    });
                        }));
    }

    public Mono<Payment> getPaymentByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Order ID cannot be null or empty"));
        }
        return paymentRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(() -> 
                    new IllegalArgumentException("Payment not found for order: " + orderId)));
    }

    public Mono<Payment> refundPayment(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return Mono.error(() -> new IllegalArgumentException("Order ID cannot be null or empty"));
        }

        return paymentRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(() -> 
                    new IllegalArgumentException("Payment not found for order: " + orderId)))
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .switchIfEmpty(Mono.error(() -> 
                    new IllegalStateException("Only completed payments can be refunded")))
                .flatMap(payment -> paymentGateway.refundPayment(payment.getId())
                        .flatMap(refunded -> {
                            if (!refunded) {
                                return Mono.error(() -> 
                                    new IllegalStateException("Refund failed for payment: " + payment.getId()));
                            }
                            return paymentRepository.updateStatus(payment.getId(), PaymentStatus.REFUNDED)
                                    .delayUntil(refundedPayment -> notificationService.createNotification(
                                            payment.getUserId(),
                                            "Refund Processed",
                                            String.format("Your refund of %s for order #%s has been processed.", 
                                                    payment.getAmount().formatWithCurrency(),
                                                    orderId),
                                            NotificationType.PAYMENT_REFUNDED,
                                            orderId));
                        }));
    }
}
