package com.inawulot.wallet.service;

import com.inawulot.wallet.exception.RateLimitException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private static final int MAX_REQUESTS = 8;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    public void check(String key) {
        Instant now = Instant.now();
        Deque<Instant> bucket = hits.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (bucket) {
            while (!bucket.isEmpty() && bucket.peekFirst().plus(WINDOW).isBefore(now)) {
                bucket.removeFirst();
            }
            if (bucket.size() >= MAX_REQUESTS) {
                throw new RateLimitException("Too many transfer attempts. Please wait before trying again.");
            }
            bucket.addLast(now);
        }
    }
}
