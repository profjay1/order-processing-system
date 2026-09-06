package com.portfolio.orderprocessing.controller;

import com.portfolio.orderprocessing.domain.Order;
import com.portfolio.orderprocessing.dto.OrderRequest;
import com.portfolio.orderprocessing.dto.OrderResponse;
import com.portfolio.orderprocessing.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and lookup")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order. Idempotent on the request's idempotencyKey field.")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.placeOrder(request);
        return ResponseEntity
                .created(URI.create("/api/v1/orders/" + order.getId()))
                .body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch an order by id")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(OrderResponse.from(orderService.getOrder(id)));
    }
}
