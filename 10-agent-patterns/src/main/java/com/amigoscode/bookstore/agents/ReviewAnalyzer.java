package com.amigoscode.bookstore.agents;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

	private final BookAssistant bookAssistant;

	public ReviewAnalyzer(BookAssistant bookAssistant) {
		this.bookAssistant = bookAssistant;
	}

	public Map<Sentiment, Long> summarize(List<String> reviews) {
		// SOLUTION-START task-3
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			var futures = reviews.stream().map(review -> executor.submit(() -> bookAssistant.classify(review))).toList();
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
		// SOLUTION-ELSE
		// TASK: // TODO task-3: classify every review with bookAssistant.classify, in parallel
		// TASK: // (one virtual thread per review), and count how many got each Sentiment
		// TASK: throw new UnsupportedOperationException("TODO task-3");
		// SOLUTION-END
	}

}
