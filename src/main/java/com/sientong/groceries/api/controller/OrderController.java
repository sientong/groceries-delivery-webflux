package com.sientong.groceries.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sientong.groceries.api.request.OrderRequest;
import com.sientong.groceries.api.response.OrderResponse;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderItem;
import com.sientong.groceries.domain.order.OrderService;
import com.sientong.groceries.domain.order.OrderStatus;
import com.sientong.groceries.domain.product.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "Order management APIs")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve a list of all orders")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class)))
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<List<OrderResponse>>> getAllOrders() {
        return orderService.getOrders()
                .map(OrderResponse::fromDomain)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @PostMapping
    @Operation(summary = "Create a new order", description = "Create a new order with the given details")
    @ApiResponse(responseCode = "201", description = "Order created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class)))
    @PreAuthorize("hasRole('CUSTOMER')")
    public Mono<ResponseEntity<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        return Flux.fromIterable(request.getItems())
            .flatMap(itemRequest -> productService.findById(itemRequest.getProductId())
                .map(product -> OrderItem.of(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    Quantity.of(itemRequest.getQuantity())
                ))
            )
            .collectList()
        .map(orderItems -> Order.builder()
            .userId(request.getUserId())
            .items(orderItems)
            .status(OrderStatus.PENDING)
            .build())
        .flatMap(orderService::createOrder)
        .map(OrderResponse::fromDomain)
        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
}

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve an order by its ID")
    @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class)))
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public Mono<ResponseEntity<OrderResponse>> getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id)
                .map(OrderResponse::fromDomain)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID", description = "Retrieve all orders for a specific user")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class)))
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public Mono<ResponseEntity<List<OrderResponse>>> getOrdersByUserId(@PathVariable String userId) {
        return orderService.getOrdersByUserId(userId)
                .map(OrderResponse::fromDomain)
                .collectList()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Update the status of an existing order")
    @ApiResponse(responseCode = "200", description = "Order status updated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class)))
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public Mono<ResponseEntity<OrderResponse>> updateOrderStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status) {
        return orderService.updateOrderStatus(id, status)
                .map(OrderResponse::fromDomain)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Retrieve all orders with a specific status")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class)))
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public Mono<ResponseEntity<List<OrderResponse>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return orderService.getOrdersByStatus(status)
                .map(OrderResponse::fromDomain)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign seller to order", description = "Assign a seller to an existing order")
    @ApiResponse(responseCode = "200", description = "Seller assigned successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderResponse.class)))
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<OrderResponse>> assignSeller(
            @PathVariable String id,
            @Valid @RequestParam String sellerId) {
        return orderService.assignSeller(id, sellerId)
                .map(OrderResponse::fromDomain)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
