package com.sientong.groceries.infrastructure.persistence.repository;

import com.sientong.groceries.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReactiveOrderItemRepository extends ReactiveCrudRepository<OrderItemEntity, String> {
    Flux<OrderItemEntity> findByOrderId(String orderId);
    
    @Query("SELECT oi.* FROM order_items oi " +
           "JOIN orders o ON o.id = oi.order_id " +
           "WHERE o.user_id = :userId")
    Flux<OrderItemEntity> findByUserId(String userId);
    
    @Query("SELECT oi.* FROM order_items oi " +
           "WHERE oi.product_id = :productId " +
           "ORDER BY oi.order_id DESC LIMIT :limit")
    Flux<OrderItemEntity> findRecentByProductId(String productId, int limit);
}
