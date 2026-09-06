# ADR-0001: Record architecture decisions

## Status
Accepted

## Context
As this system grows, future contributors (including interviewers evaluating
this project) need to understand *why* certain choices were made, not just
what the current code looks like.

## Decision
We will use lightweight Architecture Decision Records (ADRs) for any
decision with long-term structural impact: choice of messaging broker,
caching strategy, consistency model, data store, etc. Each ADR captures the
context, the decision, and the trade-offs considered.

## Consequences
- Decisions are discoverable and defensible, not just implicit in code.
- Adds small overhead per significant decision, which is worth it for
  onboarding and interview/portfolio purposes.
