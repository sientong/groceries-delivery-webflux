package com.sientong.groceries.domain.payment;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentRepository {
    Mono<Payment> save(Payment payment);
    Mono<Payment> findById(String id);
    Mono<Payment> findByOrderId(String orderId);
    Flux<Payment> findByUserId(String userId);
    Mono<Payment> updateStatus(String id, PaymentStatus status);
}
