package com.portfolio.orderprocessing.service;

import com.portfolio.orderprocessing.domain.Order;
import com.portfolio.orderprocessing.domain.OrderStatus;
import com.portfolio.orderprocessing.domain.Payment;
import com.portfolio.orderprocessing.exception.OrderNotFoundException;
import com.portfolio.orderprocessing.repository.OrderRepository;
import com.portfolio.orderprocessing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulates a payment gateway integration. In a real system this would
 * call out to Stripe/Adyen/etc. Here it deterministically fails for
 * amounts ending in .13 so the retry/DLQ path is exercisable in demos
 * and tests without external dependencies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void processPayment(UUID orderId, BigDecimal amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        boolean simulatedFailure = amount.remainder(BigDecimal.ONE)
                .compareTo(new BigDecimal("0.13")) == 0;

        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .status(simulatedFailure ? Payment.PaymentStatus.FAILED : Payment.PaymentStatus.SUCCESS)
                .failureReason(simulatedFailure ? "Simulated gateway decline" : null)
                .build();

        paymentRepository.save(payment);

        order.setStatus(simulatedFailure ? OrderStatus.PAYMENT_FAILED : OrderStatus.CONFIRMED);
        orderRepository.save(order);

        log.info("Payment {} for order {}", payment.getStatus(), orderId);
    }
}
