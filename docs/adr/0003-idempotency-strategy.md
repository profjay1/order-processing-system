# ADR-0003: Idempotency key strategy for order creation

## Status
Accepted

## Context
Clients (or the load balancer, or a flaky mobile network) may retry an
order creation request after a timeout without knowing whether the first
attempt succeeded. Without protection, this creates duplicate orders and
double-charges customers.

## Decision
Use a **client-supplied idempotency key**, validated at two layers:
1. **Redis `SETNX` with a 24h TTL** as a fast-path check — the first
   request to present a given key "claims" it.
2. **A unique constraint on `orders.idempotency_key`** in PostgreSQL as
   the authoritative fallback, since Redis is not transactionally
   consistent with the database and could theoretically be unavailable
   or evict a key early.

If a request arrives with a key that already has a matching order, we
return the *existing* order rather than erroring, so retries are
transparent to the client.

## Trade-offs
- Requires the client to generate and persist a stable key across retry
  attempts of the *same* logical action (handled in the Angular client
  by generating the key once per form submission attempt, not per HTTP
  call).
- Redis is a performance optimization, not the source of truth — the DB
  constraint is what actually guarantees correctness under all failure
  modes, including Redis being down.

## Interview talking point
"I deliberately didn't rely on Redis alone for correctness, because Redis
guarantees are best-effort under partition/failure. The database unique
constraint is the real correctness guarantee; Redis just keeps the common
case fast by avoiding a round trip to Postgres for duplicate detection."
