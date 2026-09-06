package com.portfolio.orderprocessing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Backs order idempotency with Redis SETNX semantics: the first request
 * with a given key "claims" it; subsequent requests with the same key
 * within the TTL are treated as retries, not new orders.
 *
 * The database also enforces a unique constraint on idempotency_key as a
 * second line of defense in case Redis is unavailable or evicts early.
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String KEY_PREFIX = "idempotency:order:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public boolean tryClaim(String idempotencyKey) {
        Boolean claimed = redisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + idempotencyKey, "claimed", TTL);
        return Boolean.TRUE.equals(claimed);
    }
}
