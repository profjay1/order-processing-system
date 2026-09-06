# ADR-0004: Redis caching for product catalog reads

## Status
Accepted

## Context
Product lookups (`GET /api/v1/products`, individual product fetches during
order placement) are read-heavy and change infrequently compared to how
often they're read.

## Decision
Cache product reads in Redis with a 10-minute TTL via Spring's
`@Cacheable`. Any write to stock (`InventoryService#reserveStock`) evicts
the relevant cache entries immediately (`@CacheEvict`) rather than waiting
for TTL expiry, to avoid serving stale stock counts that could lead to
overselling in the UI (though the actual reservation logic uses a
pessimistic DB lock, not the cache, so correctness never depends on cache
freshness).

## Trade-offs
- Read-your-writes consistency is preserved for the process that just
  wrote, but concurrent readers could briefly see slightly stale data
  between the write and the cache evict — acceptable for a product catalog
  display, not acceptable for the actual stock-reservation decision (which
  is why reservation bypasses the cache and reads from the DB with a lock).
- TTL was chosen empirically to balance staleness against cache hit rate;
  in production this would be tuned against actual traffic patterns and
  possibly replaced with event-driven cache invalidation.

## Interview talking point
"I was careful to separate the *cached, eventually-consistent read path*
used for display from the *transactional, strongly-consistent path* used
for the actual inventory decision — that's the difference between caching
for performance versus caching for correctness, and conflating the two is
a common source of overselling bugs."
