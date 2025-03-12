package com.sientong.groceries.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

import com.sientong.groceries.domain.notification.Notification;
import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;
import com.sientong.groceries.domain.order.DeliveryInfo;
import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderItem;
import com.sientong.groceries.domain.order.OrderRepository;
import com.sientong.groceries.domain.order.OrderService;
import com.sientong.groceries.domain.order.OrderServiceImpl;
import com.sientong.groceries.domain.order.OrderStatus;
import com.sientong.groceries.domain.product.Money;
import com.sientong.groceries.domain.product.Quantity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderServiceImpl(orderRepository, notificationService);
    }

    @Test
    void shouldCreateOrder() {
        OrderItem item = OrderItem.of(
            "1", "Organic Apples", Money.of(BigDecimal.valueOf(5.99)), Quantity.of(2)
        );

        Order order = Order.builder()
                .id("1")
                .userId("user1")
                .items(List.of(item))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(notificationService.createNotification(
                anyString(), anyString(), anyString(), any(NotificationType.class), anyString()
        )).thenReturn(Mono.just(mock(Notification.class)));

        StepVerifier.create(orderService.createOrder(order))
                .expectNext(order)
                .verifyComplete();

        verify(notificationService).createNotification(
                eq(order.getUserId()),
                eq("Order Created"),
                contains(order.getId()),
                eq(NotificationType.ORDER_CREATED),
                eq(order.getId())
        );
    }

    @Test
    void shouldUpdateOrderStatus() {
        Order order = Order.builder()
                .id("1")
                .userId("user1")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById("1")).thenReturn(Mono.just(order));
        when(orderRepository.updateStatus("1", OrderStatus.CONFIRMED))
                .thenReturn(Mono.just(order));
        when(notificationService.createNotification(
                anyString(), anyString(), anyString(), any(NotificationType.class), anyString()
        )).thenReturn(Mono.just(mock(Notification.class)));

        StepVerifier.create(orderService.updateOrderStatus("1", OrderStatus.CONFIRMED))
                .expectNext(order)
                .verifyComplete();

        verify(notificationService).createNotification(
                eq(order.getUserId()),
                eq("Order Status Updated"),
                contains(order.getId()),
                eq(NotificationType.ORDER_STATUS_UPDATED),
                eq(order.getId())
        );
    }

    @Test
    void shouldGetOrdersByUserId() {
        Order order = Order.builder()
                .id("1")
                .userId("user1")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByUserId("user1"))
                .thenReturn(Flux.just(order));

        StepVerifier.create(orderService.getOrdersByUserId("user1"))
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void shouldTrackOrder() {
        Order order = Order.builder()
                .id("1")
                .userId("user1")
                .status(OrderStatus.OUT_FOR_DELIVERY)
                .deliveryInfo(DeliveryInfo.of(
                    "123 Street", 
                    "1234567890", 
                    "TRACK123", 
                    LocalDateTime.now().plusHours(2),
                    "Leave at door"
                ))
                .build();

        when(orderRepository.findById("1")).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.trackOrder("1"))
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void shouldFailToTrackOrderWithoutDeliveryInfo() {
        Order order = Order.builder()
                .id("1")
                .userId("user1")
                .status(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById("1")).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.trackOrder("1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldFailToUpdateStatusOfDeliveredOrder() {
        Order order = Order.builder()
                .id("1")
                .userId("user1")
                .status(OrderStatus.DELIVERED)
                .build();

        when(orderRepository.findById("1")).thenReturn(Mono.just(order));

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
