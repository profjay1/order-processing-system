package com.portfolio.orderprocessing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.orderprocessing.domain.Product;
import com.portfolio.orderprocessing.dto.OrderItemRequest;
import com.portfolio.orderprocessing.dto.OrderRequest;
import com.portfolio.orderprocessing.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.data.redis.host", redis::getHost);
	registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
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

       JsonNode firstJson = objectMapper.readTree(firstResponse);
       JsonNode secondJson = objectMapper.readTree(secondResponse);
       org.assertj.core.api.Assertions.assertThat(secondJson.get("id").asText())
        .isEqualTo(firstJson.get("id").asText()); 
    }
}
