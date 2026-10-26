package com.amigoscode.bookstore.advisor;

import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Component;

/**
 * Running totals of what the app has spent on the model since it started.
 */
@Component
public class TokenUsageTracker {

	private final LongAdder calls = new LongAdder();

	private final LongAdder promptTokens = new LongAdder();

	private final LongAdder completionTokens = new LongAdder();

	public void record(long prompt, long completion) {
		calls.increment();
		promptTokens.add(prompt);
		completionTokens.add(completion);
	}

	public Totals totals() {
		return new Totals(calls.sum(), promptTokens.sum(), completionTokens.sum());
	}

	public void reset() {
		calls.reset();
		promptTokens.reset();
		completionTokens.reset();
	}

	public record Totals(long calls, long promptTokens, long completionTokens) {
	}

}
