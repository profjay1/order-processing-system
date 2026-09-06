package com.portfolio.orderprocessing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(
        @NotBlank String customerId,
        @NotBlank String idempotencyKey,
        @NotEmpty @Valid List<OrderItemRequest> items
) {}
