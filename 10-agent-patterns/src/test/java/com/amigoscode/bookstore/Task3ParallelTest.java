package com.amigoscode.bookstore;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import com.amigoscode.bookstore.agents.ReviewAnalyzer;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ Task3ParallelTest.SlowModelConfig.class, TestcontainersConfig.class })
class Task3ParallelTest {

	@Autowired
	ReviewAnalyzer analyzer;

	@Autowired
	SlowModel model;

	@Test
	void countsTheSentiments() {
		var counts = analyzer.summarize(List.of("Loved it, positive", "Great, positive", "Awful, negative"));

		assertThat(counts).isEqualTo(Map.of(Sentiment.POSITIVE, 2L, Sentiment.NEGATIVE, 1L));
	}

	@Test
	void classifiesTheReviewsInParallel() {
		model.threads.clear();
		var reviews = IntStream.range(0, 10).mapToObj(i -> "Review " + i + " positive").toList();

		long start = System.nanoTime();
		analyzer.summarize(reviews);
		long millis = (System.nanoTime() - start) / 1_000_000;

		assertThat(model.threads).as("more than one thread called the model").hasSizeGreaterThan(1);
		assertThat(millis).as("10 calls of 200ms each, at the same time").isLessThan(1500);
	}

	/**
	 * A model that takes 200ms per call, like a real one, and answers from the review text.
	 */
	static class SlowModel extends FakeChatModel {

		final Set<String> threads = Collections.synchronizedSet(new HashSet<>());

		@Override
		public ChatResponse call(Prompt prompt) {
			threads.add(Thread.currentThread().toString());
			try {
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			String review = prompt.getUserMessage().getText();
			return new FakeChatModel().reply(review.contains("negative") ? "NEGATIVE" : "POSITIVE").call(prompt);
		}

	}

	@TestConfiguration(proxyBeanMethods = false)
	static class SlowModelConfig {

		@Bean
		@Primary
		SlowModel slowModel() {
			return new SlowModel();
		}

	}

}
