package com.sientong.groceries.infrastructure.payment;

import com.sientong.groceries.domain.payment.PaymentGateway;
import com.sientong.groceries.domain.product.Money;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Component
public class SimplePaymentGateway implements PaymentGateway {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    @Override
    public Mono<Boolean> processPayment(String paymentId, Money amount) {
        return Mono.just(amount)
                .map(money -> money.getAmount().compareTo(MAX_AMOUNT) <= 0)
                .delayElement(java.time.Duration.ofMillis(500)) // Simulate processing delay
                .map(validAmount -> {
                    if (!validAmount) {
                        throw new IllegalArgumentException("Payment amount exceeds maximum limit");
                    }
                    return true;
                });
    }

    @Override
    public Mono<Boolean> refundPayment(String paymentId) {
        return Mono.just(true)
                .delayElement(java.time.Duration.ofMillis(500)) // Simulate processing delay
                .map(success -> {
                    // In a real implementation, we would validate the payment status
                    // and process the refund through a payment processor
                    return success;
                });
    }
}
