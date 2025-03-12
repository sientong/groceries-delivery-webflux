package com.sientong.groceries.service;

import com.sientong.groceries.domain.Money;
import com.sientong.groceries.domain.Quantity;
import com.sientong.groceries.domain.order.*;
import com.sientong.groceries.domain.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository, notificationService);
    }

    @Test
    void shouldCreateOrder() {
        OrderItem item = new OrderItem(
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
        when(notificationService.sendOrderNotification(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder(order))
                .expectNext(order)
                .verifyComplete();

        verify(notificationService).sendOrderNotification(eq(order), eq(OrderStatus.PENDING));
    }

    @Test
    void shouldUpdateOrderStatus() {
        Order order = Order.builder()
                .id("1")
                .userId("user1")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.updateStatus("1", OrderStatus.CONFIRMED))
                .thenReturn(Mono.just(order));
        when(notificationService.sendOrderNotification(any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderService.updateOrderStatus("1", OrderStatus.CONFIRMED))
                .expectNext(order)
                .verifyComplete();

        verify(notificationService).sendOrderNotification(eq(order), eq(OrderStatus.CONFIRMED));
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
}
