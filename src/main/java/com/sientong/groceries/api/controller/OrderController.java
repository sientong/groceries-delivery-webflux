package com.sientong.groceries.api.controller;

import com.sientong.groceries.api.request.CreateOrderRequest;
import com.sientong.groceries.api.request.UpdateDeliveryInfoRequest;
import com.sientong.groceries.domain.order.DeliveryInfo;
import com.sientong.groceries.domain.order.Order;
import com.sientong.groceries.domain.order.OrderService;
import com.sientong.groceries.domain.order.OrderStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management APIs")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("isAuthenticated()")
public class OrderController {
    private final OrderService orderService;

    @Operation(
        summary = "Create new order",
        description = "Create a new order for the authenticated user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content = @Content(schema = @Schema(implementation = Order.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Order> createOrder(
        @Parameter(description = "Order details", required = true)
        @Valid @RequestBody CreateOrderRequest request,
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        Order order = Order.builder()
                .userId(userDetails.getUsername())
                .items(request.getItems())
                .status(OrderStatus.PENDING)
                .build();
        return orderService.createOrder(order);
    }

    @Operation(
        summary = "Get user orders",
        description = "Retrieve all orders for the authenticated user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved orders",
            content = @Content(schema = @Schema(implementation = Order.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public Flux<Order> getUserOrders(
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return orderService.getOrdersByUserId(userDetails.getUsername());
    }

    @Operation(
        summary = "Get order by ID",
        description = "Retrieve a specific order by its ID"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved order",
            content = @Content(schema = @Schema(implementation = Order.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{orderId}")
    public Mono<Order> getOrder(
        @Parameter(description = "Order ID", required = true) @PathVariable String orderId,
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return orderService.getOrderById(orderId);
    }

    @Operation(
        summary = "Track order",
        description = "Track the status and delivery information of an order"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved order tracking",
            content = @Content(schema = @Schema(implementation = Order.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{orderId}/track")
    public Mono<Order> trackOrder(
        @Parameter(description = "Order ID", required = true) @PathVariable String orderId,
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return orderService.trackOrder(orderId);
    }

    @Operation(
        summary = "Checkout order",
        description = "Process order checkout and update status to CONFIRMED"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Order checkout successful",
            content = @Content(schema = @Schema(implementation = Order.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{orderId}/checkout")
    public Mono<Order> checkout(
        @Parameter(description = "Order ID", required = true) @PathVariable String orderId,
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
    }

    @Operation(
        summary = "Cancel order",
        description = "Cancel an existing order"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Order cancelled successfully",
            content = @Content(schema = @Schema(implementation = Order.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{orderId}/cancel")
    public Mono<Order> cancelOrder(
        @Parameter(description = "Order ID", required = true) @PathVariable String orderId,
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        return orderService.cancelOrder(orderId);
    }

    @Operation(
        summary = "Update delivery info",
        description = "Update delivery information for an order"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Delivery info updated successfully",
            content = @Content(schema = @Schema(implementation = Order.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{orderId}/delivery-info")
    @PreAuthorize("hasRole('SELLER')")
    public Mono<Order> updateDeliveryInfo(
        @Parameter(description = "Order ID", required = true) @PathVariable String orderId,
        @Parameter(description = "Delivery information", required = true)
        @Valid @RequestBody UpdateDeliveryInfoRequest request,
        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        DeliveryInfo deliveryInfo = DeliveryInfo.of(
            request.getAddress(),
            request.getPhone(),
            request.getTrackingNumber(),
            request.getEstimatedDeliveryTime(),
            request.getDeliveryNotes()
        );
        return orderService.updateDeliveryInfo(orderId, deliveryInfo);
    }
}
