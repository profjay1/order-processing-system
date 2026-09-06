package com.portfolio.orderprocessing.messaging.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Published after an order is persisted and inventory reserved.
 * Triggers async payment processing.
 */
public record OrderCreatedEvent(
        UUID orderId,
        String customerId,
        BigDecimal totalAmount
) implements Serializable {}
