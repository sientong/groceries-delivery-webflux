package com.sientong.groceries.infrastructure.persistence.repository;

import com.sientong.groceries.domain.payment.PaymentStatus;
import com.sientong.groceries.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactivePaymentRepository extends ReactiveCrudRepository<PaymentEntity, String> {
    Mono<PaymentEntity> findByOrderId(String orderId);
    
    Flux<PaymentEntity> findByUserId(String userId);
    
    @Modifying
    @Query("UPDATE payments SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Boolean> updateStatus(String id, PaymentStatus status);
    
    @Query("SELECT * FROM payments WHERE user_id = :userId AND status = :status")
    Flux<PaymentEntity> findByUserIdAndStatus(String userId, PaymentStatus status);
    
    @Query("SELECT * FROM payments WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    Flux<PaymentEntity> findRecentByUserId(String userId, int limit);
}
