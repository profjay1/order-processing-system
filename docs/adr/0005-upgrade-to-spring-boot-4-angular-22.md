# ADR-0005: Upgrade to Spring Boot 4 and Angular 22

## Status
Accepted

## Context
The project originally targeted Spring Boot 3.3 and Angular 18. Both
Spring Boot 4.0 and Angular 22 have since reached stable, generally
available releases with active support windows, and a portfolio project
intended to demonstrate current, hireable skills should track current
major versions rather than the versions available when the project was
first scaffolded.

## Decision
Upgrade to:
- **Spring Boot 4.0.2**, built on **Spring Framework 7** and
  **Jakarta EE 11** (Servlet 6.1 baseline). Java baseline moves to 17+;
  this project continues to target **Java 21 (LTS)**.
- **springdoc-openapi 3.0.0**, the first line to support Spring Boot 4 /
  Spring Framework 7.
- **Angular 22**, which raises the minimum Node.js version to **22.12+**.

## Rationale
- Spring Boot 4 is a major version with a defined migration path from 3.5;
  Spring's own guidance is to upgrade to the latest 3.5.x first, then move
  to 4.0 — a sequencing worth knowing even if this project jumped directly
  since it started fresh.
- Jumping straight to current major versions surfaces real migration
  concerns (Jakarta EE 11 baseline, Node 22.12 floor for Angular) that are
  worth understanding and being able to discuss, rather than sitting on
  versions that will need this same upgrade later anyway.

## Trade-offs
- Major-version bumps carry migration risk: deprecated APIs from Spring
  Boot 3.x were removed outright in 4.0, not just deprecated further, so
  a real (non-greenfield) project would need to audit for deprecated-API
  usage on 3.5 first before attempting this jump.
- Third-party dependencies (springdoc, Testcontainers, etc.) need their
  own compatible major versions — a smaller ecosystem library that hasn't
  yet published a Spring Boot 4-compatible release would block the
  upgrade entirely. This project verified springdoc-openapi's 3.0.0
  compatibility before upgrading rather than assuming it.
- Local developer environments (Java 21, Node 22.12+, Maven 3.9+) need to
  meet the new floors — see the README's "Version requirements" section.

## Interview talking point
"I don't just pin dependency versions once and forget them — I checked
that Spring Boot 4 was GA and stable, confirmed springdoc-openapi had
already shipped a compatible 3.x line before upgrading (rather than
assuming), and updated the Node.js floor in both the Dockerfile and CI
pipeline to match Angular 22's new minimum. That's the difference between
bumping a version number and actually validating an upgrade."
