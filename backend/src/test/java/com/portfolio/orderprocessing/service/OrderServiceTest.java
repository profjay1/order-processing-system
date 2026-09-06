package com.portfolio.orderprocessing.service;

import com.portfolio.orderprocessing.domain.Order;
import com.portfolio.orderprocessing.domain.OrderStatus;
import com.portfolio.orderprocessing.domain.Product;
import com.portfolio.orderprocessing.dto.OrderItemRequest;
import com.portfolio.orderprocessing.dto.OrderRequest;
import com.portfolio.orderprocessing.messaging.OrderEventPublisher;
import com.portfolio.orderprocessing.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService, with collaborators mocked. Verifies:
 *  - a fresh order is persisted with the correct computed total,
 *  - duplicate idempotency keys short-circuit to the existing order
 *    without re-reserving inventory or re-publishing events.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryService inventoryService;
    @Mock private IdempotencyService idempotencyService;
    @Mock private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = Product.builder()
                .id(productId)
                .name("Mechanical Keyboard")
                .price(new BigDecimal("89.99"))
                .stockQuantity(10)
                .build();
    }

    @Test
    void placeOrder_computesTotalAndReservesInventory() {
        OrderRequest request = new OrderRequest(
                "customer-1",
                "idem-key-1",
                List.of(new OrderItemRequest(productId, 2))
        );

        when(orderRepository.findByIdempotencyKey("idem-key-1")).thenReturn(Optional.empty());
        when(idempotencyService.tryClaim("idem-key-1")).thenReturn(true);
        when(inventoryService.getProduct(productId)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(request);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("179.98");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);
        verify(inventoryService).reserveStock(productId, 2);
        verify(eventPublisher).publishOrderCreated(any());
    }

    @Test
    void placeOrder_duplicateIdempotencyKey_returnsExistingOrderWithoutSideEffects() {
        Order existing = Order.builder()
                .id(UUID.randomUUID())
                .idempotencyKey("idem-key-1")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("179.98"))
                .build();

        OrderRequest request = new OrderRequest(
                "customer-1",
                "idem-key-1",
                List.of(new OrderItemRequest(productId, 2))
        );

        when(orderRepository.findByIdempotencyKey("idem-key-1")).thenReturn(Optional.of(existing));

        Order result = orderService.placeOrder(request);

        assertThat(result).isEqualTo(existing);
        verify(inventoryService, never()).reserveStock(any(), anyInt());
        verify(eventPublisher, never()).publishOrderCreated(any());
    }
}
