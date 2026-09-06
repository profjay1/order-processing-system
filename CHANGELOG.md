# Changelog

All notable changes to this project are documented here.
This project follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.1.0] - 2026-09-06
### Changed
- Upgraded backend to **Spring Boot 4.0.2** (Spring Framework 7, Jakarta EE 11 / Servlet 6.1 baseline).
- Upgraded `springdoc-openapi` to 3.0.0 (first line compatible with Spring Boot 4).
- Upgraded frontend to **Angular 22**; raised minimum Node.js version to 22.12+ in the frontend Dockerfile and CI pipeline.
- Bumped Testcontainers to 1.20.4.
### Added
- ADR-0005 documenting the rationale and trade-offs of the version upgrade.

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
