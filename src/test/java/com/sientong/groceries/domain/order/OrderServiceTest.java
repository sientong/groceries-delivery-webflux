package com.sientong.groceries.domain.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private List<OrderItem> items;
    private DeliveryInfo deliveryInfo;

    @BeforeEach
    void setUp() {
        items = new ArrayList<>();
        items.add(OrderItem.of(
                "prod1",
                "Apple",
                Money.of(new BigDecimal("1.50"), "USD"),
                Quantity.of(2, "kg")));

        items.add(OrderItem.of(
                "prod2",
                "Orange",
                Money.of(new BigDecimal("2.00"), "USD"),
                Quantity.of(3, "kg")));

        deliveryInfo = DeliveryInfo.of(
                "123 Main St",
                "123-456-7890",
                "TRK123",
                LocalDateTime.now().plusDays(2),
                "Leave at door");

        testOrder = Order.builder()
                .id("order1")
                .userId("user1")
                .items(items)
                .total(Money.of(new BigDecimal("9.00"), "USD"))
                .status(OrderStatus.PENDING)
                .deliveryInfo(deliveryInfo)
                .build();
    }

    @Test
    void shouldCreateOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(testOrder));
        when(notificationService.createNotification(
                eq("user1"),
                any(),
                any(),
                eq(NotificationType.ORDER_CREATED),
                eq("order1")
        )).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder(testOrder))
                .assertNext(order -> {
                    assertEquals("order1", order.getId());
                    assertEquals("user1", order.getUserId());
                    assertEquals(OrderStatus.PENDING, order.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void shouldNotCreateNullOrder() {
        StepVerifier.create(orderService.createOrder(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldUpdateOrderStatus() {
        when(orderRepository.findById("order1")).thenReturn(Mono.just(testOrder));
        when(orderRepository.updateStatus("order1", OrderStatus.PREPARING))
                .thenReturn(Mono.just(testOrder.toBuilder().status(OrderStatus.PREPARING).build()));
        when(notificationService.createNotification(
                eq("user1"),
                any(),
                any(),
                eq(NotificationType.ORDER_STATUS_UPDATED),
                eq("order1")
        )).thenReturn(Mono.empty());

        StepVerifier.create(orderService.updateOrderStatus("order1", OrderStatus.PREPARING))
                .assertNext(order -> {
                    assertEquals("order1", order.getId());
                    assertEquals(OrderStatus.PREPARING, order.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void shouldNotUpdateStatusOfCancelledOrder() {
        Order cancelledOrder = testOrder.toBuilder().status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById("order1")).thenReturn(Mono.just(cancelledOrder));

        StepVerifier.create(orderService.updateOrderStatus("order1", OrderStatus.PREPARING))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldGetOrderById() {
        when(orderRepository.findById("order1")).thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderService.getOrderById("order1"))
                .assertNext(order -> {
                    assertEquals("order1", order.getId());
                    assertEquals("user1", order.getUserId());
                })
                .verifyComplete();
    }

    @Test
    void shouldGetOrdersByUserId() {
        when(orderRepository.findByUserId("user1")).thenReturn(Flux.just(testOrder));

        StepVerifier.create(orderService.getOrdersByUserId("user1"))
                .assertNext(order -> {
                    assertEquals("order1", order.getId());
                    assertEquals("user1", order.getUserId());
                })
                .verifyComplete();
    }

    @Test
    void shouldTrackOrder() {
        when(orderRepository.findById("order1")).thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderService.trackOrder("order1"))
                .assertNext(order -> {
                    assertEquals("order1", order.getId());
                    assertEquals("TRK123", order.getDeliveryInfo().getTrackingNumber());
                })
                .verifyComplete();
    }

    @Test
    void shouldNotTrackOrderWithoutDeliveryInfo() {
        Order orderWithoutDelivery = testOrder.toBuilder().deliveryInfo(null).build();
        when(orderRepository.findById("order1")).thenReturn(Mono.just(orderWithoutDelivery));

        StepVerifier.create(orderService.trackOrder("order1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldUpdateDeliveryInfo() {
        DeliveryInfo newDeliveryInfo = DeliveryInfo.of(
                "456 Oak St",
                "987-654-3210",
                "TRK456",
                LocalDateTime.now().plusDays(1),
                "Ring doorbell");

        when(orderRepository.findById("order1")).thenReturn(Mono.just(testOrder));
        when(orderRepository.updateDeliveryInfo("order1", newDeliveryInfo))
                .thenReturn(Mono.just(testOrder.toBuilder().deliveryInfo(newDeliveryInfo).build()));
        when(notificationService.createNotification(
                eq("user1"),
                any(),
                any(),
                eq(NotificationType.DELIVERY_UPDATE),
                eq("order1")
        )).thenReturn(Mono.empty());

        StepVerifier.create(orderService.updateDeliveryInfo("order1", newDeliveryInfo))
                .assertNext(order -> {
                    assertEquals("456 Oak St", order.getDeliveryInfo().getAddress());
                    assertEquals("TRK456", order.getDeliveryInfo().getTrackingNumber());
                })
                .verifyComplete();
    }

    @Test
    void shouldGetOrdersByStatus() {
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(Flux.just(testOrder));

        StepVerifier.create(orderService.getOrdersByStatus(OrderStatus.PENDING))
                .assertNext(order -> {
                    assertEquals("order1", order.getId());
                    assertEquals(OrderStatus.PENDING, order.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void shouldCancelOrder() {
        when(orderRepository.findById("order1")).thenReturn(Mono.just(testOrder));
        when(orderRepository.updateStatus("order1", OrderStatus.CANCELLED))
                .thenReturn(Mono.just(testOrder.toBuilder().status(OrderStatus.CANCELLED).build()));
        when(notificationService.createNotification(
                eq("user1"),
                any(),
                any(),
                eq(NotificationType.ORDER_CANCELLED),
                eq("order1")
        )).thenReturn(Mono.empty());

        StepVerifier.create(orderService.cancelOrder("order1"))
                .assertNext(order -> {
                    assertEquals("order1", order.getId());
                    assertEquals(OrderStatus.CANCELLED, order.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void shouldNotCancelDeliveredOrder() {
        Order deliveredOrder = testOrder.toBuilder().status(OrderStatus.DELIVERED).build();
        when(orderRepository.findById("order1")).thenReturn(Mono.just(deliveredOrder));

        StepVerifier.create(orderService.cancelOrder("order1"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldGetAllOrders() {
        when(orderRepository.findAll()).thenReturn(Flux.just(testOrder));

        StepVerifier.create(orderService.getOrders())
                .assertNext(order -> {
                    assertEquals("order1", order.getId());
                    assertEquals("user1", order.getUserId());
                })
                .verifyComplete();
    }
}
