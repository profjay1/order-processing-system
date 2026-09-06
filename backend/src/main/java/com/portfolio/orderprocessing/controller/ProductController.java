package com.portfolio.orderprocessing.controller;

import com.portfolio.orderprocessing.dto.ProductResponse;
import com.portfolio.orderprocessing.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog (read-only, Redis-cached)")
public class ProductController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<ProductResponse> listProducts() {
        return inventoryService.getAllProducts().stream()
                .map(ProductResponse::from)
                .toList();
    }
}
