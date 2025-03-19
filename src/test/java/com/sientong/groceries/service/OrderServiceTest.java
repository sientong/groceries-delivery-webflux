package com.sientong.groceries.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.notification.MockableNotification;
import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;
import com.sientong.groceries.domain.order.DeliveryInfo;
import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderItem;
import com.sientong.groceries.domain.order.OrderRepository;
import com.sientong.groceries.domain.order.OrderService;
import com.sientong.groceries.domain.order.OrderServiceImpl;
import com.sientong.groceries.domain.order.OrderStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    private OrderService orderService;
    private Order testOrder;
    private OrderItem testItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderServiceImpl(orderRepository, notificationService);
        
        testItem = OrderItem.of(
            "1", "Organic Apples", Money.of(BigDecimal.valueOf(5.99)), Quantity.of(2)
        );

        testOrder = Order.builder()
                .id("1")
                .userId("user1")
                .items(List.of(testItem))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(testOrder));
        when(notificationService.createNotification(
                anyString(), anyString(), anyString(), any(NotificationType.class), anyString()
        )).thenReturn(Mono.just(new MockableNotification()));

        StepVerifier.create(orderService.createOrder(testOrder))
                .expectNext(testOrder)
                .verifyComplete();

        verify(notificationService).createNotification(
                eq(testOrder.getUserId()),
                eq("Order Created"),
                contains(testOrder.getId()),
                eq(NotificationType.ORDER_CREATED),
                eq(testOrder.getId())
        );
    }

    @Test
    void shouldUpdateOrderStatus() {
        Order updatedOrder = Order.builder()
                .id(testOrder.getId())
                .userId(testOrder.getUserId())
                .items(testOrder.getItems())
                .status(OrderStatus.CONFIRMED)
                .createdAt(testOrder.getCreatedAt())
                .build();

        when(orderRepository.findById("1")).thenReturn(Mono.just(testOrder));
        when(orderRepository.updateStatus("1", OrderStatus.CONFIRMED))
                .thenReturn(Mono.just(updatedOrder));
        when(notificationService.createNotification(
                anyString(), anyString(), anyString(), any(NotificationType.class), anyString()
        )).thenReturn(Mono.just(new MockableNotification()));

        StepVerifier.create(orderService.updateOrderStatus("1", OrderStatus.CONFIRMED))
                .expectNext(updatedOrder)
                .verifyComplete();

        verify(notificationService).createNotification(
                eq(testOrder.getUserId()),
                eq("Order Status Updated"),
                contains(testOrder.getId()),
                eq(NotificationType.ORDER_STATUS_UPDATED),
                eq(testOrder.getId())
        );
    }

    @Test
    void shouldGetOrdersByUserId() {
        when(orderRepository.findByUserId("user1"))
                .thenReturn(Flux.just(testOrder));

        StepVerifier.create(orderService.getOrdersByUserId("user1"))
                .expectNext(testOrder)
                .verifyComplete();
    }

    @Test
    void shouldTrackOrder() {
        Order orderWithDelivery = Order.builder()
                .id(testOrder.getId())
                .userId(testOrder.getUserId())
                .items(testOrder.getItems())
                .status(OrderStatus.OUT_FOR_DELIVERY)
                .deliveryInfo(DeliveryInfo.of(
                    "123 Street", 
                    "1234567890", 
                    "TRACK123", 
                    LocalDateTime.now().plusHours(2),
                    "Leave at door"
                ))
                .createdAt(testOrder.getCreatedAt())
                .build();

        when(orderRepository.findById("1")).thenReturn(Mono.just(orderWithDelivery));

        StepVerifier.create(orderService.trackOrder("1"))
                .expectNext(orderWithDelivery)
                .verifyComplete();
    }

    @Test
    void shouldFailToTrackOrderWithoutDeliveryInfo() {
        when(orderRepository.findById("1")).thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderService.trackOrder("1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldFailToUpdateStatusOfDeliveredOrder() {
        Order deliveredOrder = Order.builder()
                .id(testOrder.getId())
                .userId(testOrder.getUserId())
                .items(testOrder.getItems())
                .status(OrderStatus.DELIVERED)
                .createdAt(testOrder.getCreatedAt())
                .build();

        when(orderRepository.findById("1")).thenReturn(Mono.just(deliveredOrder));

        StepVerifier.create(orderService.updateOrderStatus("1", OrderStatus.CANCELLED))
                .expectError(IllegalStateException.class)
                .verify();

        verify(orderRepository, never()).updateStatus(anyString(), any(OrderStatus.class));
        verify(notificationService, never()).createNotification(
                anyString(), anyString(), anyString(), any(NotificationType.class), anyString()
        );
    }

    @Test
    void shouldFailToUpdateStatusWithNullOrderId() {
        StepVerifier.create(orderService.updateOrderStatus(null, OrderStatus.CONFIRMED))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(orderRepository, never()).updateStatus(anyString(), any(OrderStatus.class));
        verify(notificationService, never()).createNotification(
                anyString(), anyString(), anyString(), any(NotificationType.class), anyString()
        );
    }
}
