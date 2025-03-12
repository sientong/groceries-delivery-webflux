package com.sientong.groceries.domain.payment;

import com.sientong.groceries.domain.product.Money;
import reactor.core.publisher.Mono;

public interface PaymentGateway {
    Mono<Boolean> processPayment(String paymentId, Money amount);
    Mono<Boolean> refundPayment(String paymentId);
}
