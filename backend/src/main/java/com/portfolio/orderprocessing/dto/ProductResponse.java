package com.portfolio.orderprocessing.dto;

import com.portfolio.orderprocessing.domain.Product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        Integer stockQuantity
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStockQuantity());
    }
}
