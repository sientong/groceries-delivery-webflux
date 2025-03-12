package com.sientong.groceries.infrastructure.persistence.repository;

import com.sientong.groceries.domain.order.OrderStatus;
import com.sientong.groceries.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveOrderRepository extends ReactiveCrudRepository<OrderEntity, String> {
    Flux<OrderEntity> findByUserId(String userId);
    
    Flux<OrderEntity> findByStatus(OrderStatus status);
    
    @Modifying
    @Query("UPDATE orders SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Boolean> updateStatus(String id, OrderStatus status);
    
    @Modifying
    @Query("UPDATE orders SET delivery_address = :address, delivery_phone = :phone, " +
           "tracking_number = :trackingNumber, estimated_delivery_time = :estimatedTime, " +
           "delivery_notes = :notes, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Boolean> updateDeliveryInfo(String id, String address, String phone, 
                                   String trackingNumber, String estimatedTime, String notes);

    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    Flux<OrderEntity> findRecentOrdersByUserId(String userId, int limit);
}
