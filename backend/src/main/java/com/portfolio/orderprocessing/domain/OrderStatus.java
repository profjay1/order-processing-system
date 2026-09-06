package com.portfolio.orderprocessing.domain;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    PAYMENT_PROCESSING,
    PAYMENT_FAILED,
    CONFIRMED,
    CANCELLED
}
