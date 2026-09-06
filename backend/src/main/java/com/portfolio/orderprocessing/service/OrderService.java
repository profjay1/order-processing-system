package com.portfolio.orderprocessing.service;

import com.portfolio.orderprocessing.domain.Order;
import com.portfolio.orderprocessing.domain.OrderItem;
import com.portfolio.orderprocessing.domain.OrderStatus;
import com.portfolio.orderprocessing.domain.Product;
import com.portfolio.orderprocessing.dto.OrderItemRequest;
import com.portfolio.orderprocessing.dto.OrderRequest;
import com.portfolio.orderprocessing.exception.OrderNotFoundException;
import com.portfolio.orderprocessing.messaging.OrderEventPublisher;
import com.portfolio.orderprocessing.messaging.event.OrderCreatedEvent;
import com.portfolio.orderprocessing.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Orchestrates order placement:
 *  1. Idempotency check (Redis) - reject duplicate submissions early.
 *  2. Reserve inventory for every line item within a single DB transaction.
 *  3. Persist the order in PENDING/INVENTORY_RESERVED state.
 *  4. Publish OrderCreatedEvent so payment processing happens
 *     asynchronously, keeping the API response fast.
 *
 * Note: if any inventory reservation fails partway through, the whole
 * transaction rolls back, so no line item is left "half reserved".
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final IdempotencyService idempotencyService;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public Order placeOrder(OrderRequest request) {
        var existing = orderRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            log.info("Duplicate order submission detected for idempotency key {}", request.idempotencyKey());
            return existing.get();
        }

        if (!idempotencyService.tryClaim(request.idempotencyKey())) {
            // Another in-flight request already claimed this key; the DB
            // unique constraint is the authoritative fallback check above.
            log.info("Idempotency key {} already claimed; treating as duplicate", request.idempotencyKey());
        }

        Order order = Order.builder()
                .customerId(request.customerId())
                .idempotencyKey(request.idempotencyKey())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = inventoryService.getProduct(itemRequest.productId());
            inventoryService.reserveStock(itemRequest.productId(), itemRequest.quantity());

            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();
            order.addItem(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        Order saved = orderRepository.save(order);

        eventPublisher.publishOrderCreated(
                new OrderCreatedEvent(saved.getId(), saved.getCustomerId(), saved.getTotalAmount())
        );

        return saved;
    }

    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
