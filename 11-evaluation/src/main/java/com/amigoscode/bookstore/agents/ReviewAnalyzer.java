package com.amigoscode.bookstore.agents;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.amigoscode.bookstore.BookAssistant;
import com.amigoscode.bookstore.Sentiment;
import org.springframework.stereotype.Service;

/**
 * Pattern 3: parallelization. Independent pieces of work run at the same time, then the
 * results are combined. 50 reviews take as long as the slowest one, not 50 times as long.
 */
@Service
public class ReviewAnalyzer {

	/**
	 * Every review is a paid model call. Without limits, one request could cost a fortune
	 * or hit DeepSeek's rate limit.
	 */
	static final int MAX_REVIEWS = 50;

	static final int MAX_PARALLEL_CALLS = 10;

	private final Semaphore permits = new Semaphore(MAX_PARALLEL_CALLS);

	private final BookAssistant bookAssistant;

	public ReviewAnalyzer(BookAssistant bookAssistant) {
		this.bookAssistant = bookAssistant;
	}

	/**
	 * At most MAX_PARALLEL_CALLS model calls at the same time, however many threads are waiting.
	 */
	private Sentiment classifyWithPermit(String review) throws InterruptedException {
		permits.acquire();
		try {
			return bookAssistant.classify(review);
		}
		finally {
			permits.release();
		}
	}

	public Map<Sentiment, Long> summarize(List<String> reviews) {
		if (reviews.size() > MAX_REVIEWS) {
			throw new IllegalArgumentException("At most " + MAX_REVIEWS + " reviews per request");
		}
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			var futures = reviews.stream().map(review -> executor.submit(() -> classifyWithPermit(review))).toList();
			return futures.stream()
				.map(future -> {
					try {
						return future.get();
					}
					catch (Exception e) {
						throw new IllegalStateException("Could not classify a review", e);
					}
				})
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		}
	}

}
