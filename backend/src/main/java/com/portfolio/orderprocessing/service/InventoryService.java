package com.portfolio.orderprocessing.service;

import com.portfolio.orderprocessing.domain.Product;
import com.portfolio.orderprocessing.exception.InsufficientStockException;
import com.portfolio.orderprocessing.exception.ProductNotFoundException;
import com.portfolio.orderprocessing.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#id")
    public Product getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Cacheable(value = "products:all")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Reserves stock for a single product within the caller's transaction.
     * Uses a pessimistic lock (see ProductRepository#findByIdForUpdate) so
     * concurrent reservations for the same product serialize correctly
     * instead of both reading pre-decrement stock and overselling.
     */
    @Transactional
    @CacheEvict(value = {"products", "products:all"}, allEntries = true)
    public void reserveStock(UUID productId, int quantity) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + productId +
                            ": requested " + quantity + ", available " + product.getStockQuantity());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }
}
