package com.sientong.groceries.infrastructure.persistence.adapter;

import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderRepository;
import com.sientong.groceries.domain.order.OrderStatus;
import com.sientong.groceries.domain.order.DeliveryInfo;
import com.sientong.groceries.infrastructure.persistence.entity.OrderEntity;
import com.sientong.groceries.infrastructure.persistence.entity.OrderItemEntity;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveOrderRepository;
import com.sientong.groceries.infrastructure.persistence.repository.ReactiveOrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {
    private final ReactiveOrderRepository orderRepository;
    private final ReactiveOrderItemRepository orderItemRepository;

    @Override
    public Mono<Order> save(Order order) {
        OrderEntity orderEntity = OrderEntity.fromDomain(order);
        return orderRepository.save(orderEntity)
                .flatMap(savedOrder -> Flux.fromIterable(order.getItems())
                        .map(item -> OrderItemEntity.fromDomain(savedOrder.getId(), item))
                        .flatMap(orderItemRepository::save)
                        .collectList()
                        .map(items -> {
                            savedOrder.setItems(items);
                            return savedOrder.toDomain();
                        }));
    }

    @Override
    public Mono<Order> findById(String id) {
        return orderRepository.findById(id)
                .flatMap(order -> orderItemRepository.findByOrderId(id)
                        .collectList()
                        .map(items -> {
                            order.setItems(items);
                            return order.toDomain();
                        }));
    }

    @Override
    public Flux<Order> findByUserId(String userId) {
        return orderRepository.findByUserId(userId)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .map(items -> {
                            order.setItems(items);
                            return order.toDomain();
                        }));
    }

    @Override
    public Mono<Order> updateStatus(String id, OrderStatus status) {
        return orderRepository.updateStatus(id, status)
                .filter(updated -> updated)
                .flatMap(updated -> findById(id));
    }

    @Override
    public Mono<Order> updateDeliveryInfo(String id, DeliveryInfo deliveryInfo) {
        return orderRepository.updateDeliveryInfo(
                id,
                deliveryInfo.getAddress(),
                deliveryInfo.getPhone(),
                deliveryInfo.getTrackingNumber(),
                deliveryInfo.getEstimatedDeliveryTime().toString(),
                deliveryInfo.getDeliveryNotes()
            )
            .filter(updated -> updated)
            .flatMap(updated -> findById(id));
    }

    @Override
    public Flux<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .map(items -> {
                            order.setItems(items);
                            return order.toDomain();
                        }));
    }
}
