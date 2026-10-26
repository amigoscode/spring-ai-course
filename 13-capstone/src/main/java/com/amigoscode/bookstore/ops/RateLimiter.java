package com.amigoscode.bookstore.ops;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * At most MAX_REQUESTS model-backed requests per user per minute. Stops one user (or one
 * leaked password) from running up the bill or using all of DeepSeek's rate limit.
 * In memory, so it's per instance: with several instances you'd keep the counts in Redis.
 */
@Component
public class RateLimiter {

	public static final int MAX_REQUESTS = 10;

	static final Duration WINDOW = Duration.ofMinutes(1);

	private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();

	private final Clock clock;

	public RateLimiter() {
		this(Clock.systemUTC());
	}

	public RateLimiter(Clock clock) {
		this.clock = clock;
	}

	/**
	 * True if the user may make another request now, and counts it.
	 */
	public boolean tryAcquire(String user) {
		Instant now = clock.instant();
		Deque<Instant> times = requests.computeIfAbsent(user, u -> new ArrayDeque<>());
		synchronized (times) {
			while (!times.isEmpty() && times.peekFirst().isBefore(now.minus(WINDOW))) {
				times.removeFirst();
			}
			if (times.size() >= MAX_REQUESTS) {
				return false;
			}
			times.addLast(now);
			return true;
		}
	}

	public void reset() {
		requests.clear();
	}

}
