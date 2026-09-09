package com.portfolio.orderprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.resilience.annotation.EnableResilientMethods;

/**
 * Entry point for the Order Processing System.
 *
 * This service demonstrates a production-shaped order pipeline:
 * order placement -> inventory reservation -> async payment processing ->
 * event-driven status updates, backed by idempotency keys, Redis caching,
 * and RabbitMQ messaging with retry/DLQ handling.
 */
@SpringBootApplication
@EnableCaching
@EnableResilientMethods
public class OrderProcessingApplication {

    public static void main(String[] args) {

        SpringApplication.run(OrderProcessingApplication.class, args);
    }
}
