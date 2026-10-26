package com.amigoscode.bookstore.agents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

/**
 * Pattern 5: evaluator-optimizer. One call writes, another call judges against clear
 * criteria, and the writer tries again with the feedback. Stop after a few rounds:
 * a loop without a limit is a loop without a budget.
 */
@Service
public class BlurbOptimizer {

	private static final Logger log = LoggerFactory.getLogger(BlurbOptimizer.class);

	static final int MAX_ROUNDS = 3;

	static final String CRITERIA = """
			- at most 40 words
			- mentions the book's title
			- says who the book is for
			- no clichés like "must-read" or "page-turner\"""";

	private final ChatClient writer;

	private final ChatClient evaluator;

	public BlurbOptimizer(ChatClient.Builder chatClientBuilder) {
		this.writer = chatClientBuilder.clone().defaultOptions(ChatOptions.builder().temperature(0.9)).build();
		this.evaluator = chatClientBuilder.clone().defaultOptions(ChatOptions.builder().temperature(0.0)).build();
	}

	public Result write(String bookTitle) {
		// TODO task-5: up to MAX_ROUNDS times: the writer writes a blurb (with the previous attempt
		// and the feedback), the evaluator returns an Evaluation. Stop as soon as it's PASS.
		// If no round passes, return the last blurb with accepted = false.
		throw new UnsupportedOperationException("TODO task-5");
	}

	public enum Verdict {

		PASS, NEEDS_IMPROVEMENT

	}

	public record Evaluation(Verdict verdict, String feedback) {
	}

	public record Result(String blurb, int rounds, boolean accepted) {
	}

}
