package com.sientong.groceries.infrastructure.persistence.adapter;

import com.sientong.groceries.domain.payment.Payment;
import com.sientong.groceries.domain.payment.PaymentRepository;
import com.sientong.groceries.domain.payment.PaymentStatus;
import com.sientong.groceries.infrastructure.persistence.entity.PaymentEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactivePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {
    private final ReactivePaymentRepository reactivePaymentRepository;

    @Override
    public Mono<Payment> save(Payment payment) {
        return reactivePaymentRepository.save(PaymentEntity.fromDomain(payment))
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Mono<Payment> findById(String id) {
        return reactivePaymentRepository.findById(id)
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Mono<Payment> findByOrderId(String orderId) {
        return reactivePaymentRepository.findByOrderId(orderId)
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Flux<Payment> findByUserId(String userId) {
        return reactivePaymentRepository.findByUserId(userId)
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Mono<Payment> updateStatus(String id, PaymentStatus status) {
        return reactivePaymentRepository.updateStatus(id, status)
                .filter(updated -> updated)
                .flatMap(updated -> reactivePaymentRepository.findById(id))
                .map(PaymentEntity::toDomain);
    }
}
