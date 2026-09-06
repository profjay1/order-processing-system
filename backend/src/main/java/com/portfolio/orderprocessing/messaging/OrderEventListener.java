package com.portfolio.orderprocessing.messaging;

import com.portfolio.orderprocessing.messaging.event.OrderCreatedEvent;
import com.portfolio.orderprocessing.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Consumes OrderCreatedEvent and triggers payment processing asynchronously.
 * Retries transient failures up to 3 times with exponential backoff before
 * the message is routed to the dead-letter queue by RabbitMQ's DLX config
 * (see RabbitConfig). This decouples order creation latency from the
 * downstream payment provider's response time.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = "order.created.queue")
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Processing payment for order {}", event.orderId());
        paymentService.processPayment(event.orderId(), event.totalAmount());
    }
}
