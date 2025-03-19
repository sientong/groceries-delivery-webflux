package com.sientong.groceries.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.sientong.groceries.api.controller.OrderController;
import com.sientong.groceries.api.request.OrderItemRequest;
import com.sientong.groceries.api.request.OrderRequest;
import com.sientong.groceries.config.TestSecurityConfig;
import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.order.DeliveryInfo;
import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderItem;
import com.sientong.groceries.domain.order.OrderService;
import com.sientong.groceries.domain.order.OrderStatus;
import com.sientong.groceries.domain.product.Category;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(OrderController.class)
@Import(TestSecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ProductService productService;

    private Order testOrder;
    private List<OrderItem> items;
    private OrderRequest testOrderRequest;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        Category fruitCategory = Category.of("fruits", "Fruits");

        testProduct1 = Product.builder()
                .id("prod1")
                .name("Apple")
                .description("Fresh apple")
                .price(Money.of(new BigDecimal("1.50"), "USD"))
                .category(fruitCategory)
                .quantity(Quantity.of(100, "kg"))
                .imageUrl("http://example.com/apple.jpg")
                .build();

        testProduct2 = Product.builder()
                .id("prod2")
                .name("Orange")
                .description("Fresh orange")
                .price(Money.of(new BigDecimal("2.00"), "USD"))
                .category(fruitCategory)
                .quantity(Quantity.of(100, "kg"))
                .imageUrl("http://example.com/orange.jpg")
                .build();

        when(productService.findById("prod1")).thenReturn(Mono.just(testProduct1));
        when(productService.findById("prod2")).thenReturn(Mono.just(testProduct2));

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

        DeliveryInfo deliveryInfo = DeliveryInfo.of(
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

        List<OrderItemRequest> itemRequests = new ArrayList<>();
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId("prod1");
        item1.setQuantity(2);
        itemRequests.add(item1);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId("prod2");
        item2.setQuantity(3);
        itemRequests.add(item2);

        testOrderRequest = new OrderRequest();
        testOrderRequest.setUserId("user1");
        testOrderRequest.setItems(itemRequests);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDenyGetAllOrdersForAdmin() {
        when(orderService.getOrders()).thenReturn(Flux.just(testOrder));

        webTestClient.get()
                .uri("/api/v1/orders")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldCreateOrder() {
        when(orderService.createOrder(any())).thenReturn(Mono.just(testOrder));

        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testOrderRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("order1")
                .jsonPath("$.userId").isEqualTo("user1")
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.items.length()").isEqualTo(2)
                .jsonPath("$.items[0].productId").isEqualTo("prod1")
                .jsonPath("$.items[0].productName").isEqualTo("Apple")
                .jsonPath("$.items[0].quantity.value").isEqualTo(2)
                .jsonPath("$.items[0].quantity.unit").isEqualTo("kg")
                .jsonPath("$.items[1].productId").isEqualTo("prod2")
                .jsonPath("$.items[1].productName").isEqualTo("Orange")
                .jsonPath("$.items[1].quantity.value").isEqualTo(3)
                .jsonPath("$.items[1].quantity.unit").isEqualTo("kg");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetOrderById() {
        when(orderService.getOrderById("order1")).thenReturn(Mono.just(testOrder));

        webTestClient.get()
                .uri("/api/v1/orders/order1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("order1")
                .jsonPath("$.userId").isEqualTo("user1")
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.items.length()").isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturn404WhenOrderNotFound() {
        when(orderService.getOrderById("nonexistent")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/orders/nonexistent")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetOrdersByUserId() {
        when(orderService.getOrdersByUserId("user1")).thenReturn(Flux.just(testOrder));

        webTestClient.get()
                .uri("/api/v1/orders/user/user1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("order1")
                .jsonPath("$[0].userId").isEqualTo("user1")
                .jsonPath("$[0].status").isEqualTo("PENDING");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDenyUpdateOrderStatusForAdmin() {
        when(orderService.updateOrderStatus(eq("order1"), eq(OrderStatus.PREPARING)))
                .thenReturn(Mono.just(testOrder));

        webTestClient.patch()
                .uri("/api/v1/orders/order1/status?status=PREPARING")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() {
        webTestClient.get()
                .uri("/api/v1/orders")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturn403WhenNotAdmin() {
        webTestClient.get()
                .uri("/api/v1/orders")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }
}
