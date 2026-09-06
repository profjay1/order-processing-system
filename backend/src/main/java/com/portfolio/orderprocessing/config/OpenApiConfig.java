package com.portfolio.orderprocessing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderProcessingOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Order Processing System API")
                .description("Distributed order processing pipeline with async payment and inventory workflows")
                .version("1.0.0")
                .contact(new Contact().name("Portfolio Project").url("https://github.com/yourusername/order-processing-system")));
    }
}
