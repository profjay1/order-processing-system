# Changelog

All notable changes to this project are documented here.
This project follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.0] - 2026-09-06
### Added
- Order placement API with idempotency-key support (Redis + DB unique constraint).
- Inventory reservation with pessimistic locking to prevent overselling.
- Asynchronous payment processing via RabbitMQ with retry + dead-letter queue.
- Product catalog endpoint with Redis-backed caching.
- Angular frontend: place order form, order status page with live polling, product list.
- PostgreSQL schema managed via Flyway migrations.
- Testcontainers-based integration tests covering the full order flow.
- Docker Compose environment (Postgres, Redis, RabbitMQ, backend, frontend).
- GitHub Actions CI for backend (Maven/JUnit/JaCoCo) and frontend (Karma/Jasmine).
- Actuator + Micrometer/Prometheus metrics endpoint.
- OpenAPI/Swagger documentation.
