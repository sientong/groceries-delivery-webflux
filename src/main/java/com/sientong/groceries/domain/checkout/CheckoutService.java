package com.sientong.groceries.domain.checkout;

import org.springframework.stereotype.Service;

import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;
import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderStatus;
import com.sientong.groceries.domain.payment.PaymentService;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final ProductRepository productRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public Mono<Order> processCheckout(Order order) {
        return validateOrder(order)
                .flatMap(validOrder -> processPayment(validOrder)
                        .flatMap(this::updateInventory)
                        .flatMap(this::sendNotifications));
    }

    private Mono<Order> validateOrder(Order order) {
        return Mono.just(order)
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .switchIfEmpty(Mono.error(() -> new IllegalStateException("Order must be in PENDING status")))
                .flatMap(this::validateInventory);
    }

    private Mono<Order> validateInventory(Order order) {
        return Mono.just(order)
                .flatMap(o -> Flux.fromIterable(o.getItems())
                        .<Product>flatMap(item -> productRepository.findById(item.getProductId())
                                .flatMap(product -> {
                                    if (product.getQuantity().getValue() >= item.getQuantity().getValue()) {
                                        return Mono.just(product);
                                    }
                                    return Mono.error(() -> new IllegalStateException(
                                            String.format("Insufficient inventory for product %s: requested %d, available %d",
                                                    item.getProductId(),
                                                    item.getQuantity().getValue(),
                                                    product.getQuantity().getValue())));
                                }))
                        .collectList()
                        .thenReturn(order));
    }

    private Mono<Tuple2<Order, Boolean>> processPayment(Order order) {
        return paymentService.processPayment(order)
                .map(paymentSuccess -> Tuples.of(order, paymentSuccess))
                .flatMap(tuple -> {
                    if (!tuple.getT2()) {
                        return Mono.error(() -> new PaymentException("Payment processing failed"));
                    }
                    return Mono.just(tuple);
                });
    }

    private Mono<Order> updateInventory(Tuple2<Order, Boolean> orderAndPayment) {
        Order order = orderAndPayment.getT1();
        return Flux.fromIterable(order.getItems())
                .<Product>flatMap(item -> productRepository.updateStock(
                        item.getProductId(),
                        Quantity.of(-item.getQuantity().getValue())
                ))
                .collectList()
                .thenReturn(order)
                .map(o -> {
                    o.updateStatus(OrderStatus.CONFIRMED);
                    return o;
                });
    }

    private Mono<Order> sendNotifications(Order order) {
        return notificationService.createNotification(
                order.getUserId(),
                "Order Confirmed",
                "Your order #" + order.getId() + " has been confirmed and is being processed.",
                NotificationType.ORDER_STATUS_UPDATED,
                order.getId()
        ).thenReturn(order);
    }

    public static class PaymentException extends RuntimeException {
        public PaymentException(String message) {
            super(message);
        }
    }
}
