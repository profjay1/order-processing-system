package com.portfolio.orderprocessing.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * Available stock. Reservation decrements this within a single
     * transaction using a pessimistic lock to avoid overselling under
     * concurrent requests (see OrderService#reserveInventory).
     */
    @Column(nullable = false)
    private Integer stockQuantity;
}
