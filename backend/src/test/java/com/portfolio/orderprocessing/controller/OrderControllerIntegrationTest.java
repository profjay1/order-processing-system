package com.portfolio.orderprocessing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.orderprocessing.domain.Product;
import com.portfolio.orderprocessing.dto.OrderItemRequest;
import com.portfolio.orderprocessing.dto.OrderRequest;
import com.portfolio.orderprocessing.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end test that spins up real Postgres and RabbitMQ containers via
 * Testcontainers, exercises the full HTTP -> service -> DB -> broker path,
 * and asserts the order is created with the correct computed total and
 * that a duplicate request (same idempotency key) does not create a
 * second row.
 *
 * Note: Redis is assumed available on localhost for idempotency claims
 * in this test profile; swap for a Testcontainers Redis module if you
 * want full container isolation in CI.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class OrderControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("orderdb")
            .withUsername("orderuser")
            .withPassword("orderpass");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;

    @Test
    void placeOrder_persistsOrderAndReturns201() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Test Widget")
                .price(new BigDecimal("25.00"))
                .stockQuantity(5)
                .build());

        OrderRequest request = new OrderRequest(
                "customer-42",
                "integration-test-key-1",
                List.of(new OrderItemRequest(product.getId(), 2))
        );

        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("customer-42"))
                .andExpect(jsonPath("$.totalAmount").value(50.00));
    }

    @Test
    void placeOrder_duplicateIdempotencyKey_doesNotCreateSecondOrder() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Test Gadget")
                .price(new BigDecimal("10.00"))
                .stockQuantity(10)
                .build());

        OrderRequest request = new OrderRequest(
                "customer-99",
                "integration-test-key-dup",
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        String firstResponse = mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(firstResponse).isEqualTo(secondResponse);
    }
}
