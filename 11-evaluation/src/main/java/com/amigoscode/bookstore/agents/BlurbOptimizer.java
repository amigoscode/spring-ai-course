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
		String blurb = null;
		String feedback = "none yet";
		for (int round = 1; round <= MAX_ROUNDS; round++) {
			String previous = blurb == null ? "none" : blurb;
			String lastFeedback = feedback;
			blurb = writer.prompt()
				.user(u -> u.text("""
						Write a marketing blurb for the book "{title}".
						Criteria:
						{criteria}
						Previous attempt: {previous}
						Feedback on it: {feedback}
						Reply with the blurb only.""").param("title", bookTitle).param("criteria", CRITERIA)
					.param("previous", previous).param("feedback", lastFeedback))
				.call()
				.content();

			String candidate = blurb;
			Evaluation evaluation = evaluator.prompt()
				.user(u -> u.text("""
						Evaluate this blurb for the book "{title}" against the criteria.
						Criteria:
						{criteria}
						Blurb: {blurb}""").param("title", bookTitle).param("criteria", CRITERIA).param("blurb", candidate))
				.call()
				.entity(Evaluation.class);

			log.info("Round {}: {} {}", round, evaluation.verdict(), evaluation.feedback());
			if (evaluation.verdict() == Verdict.PASS) {
				return new Result(blurb, round, true);
			}
			feedback = evaluation.feedback();
		}
		return new Result(blurb, MAX_ROUNDS, false);
	}

	public enum Verdict {

		PASS, NEEDS_IMPROVEMENT

	}

	public record Evaluation(Verdict verdict, String feedback) {
	}

	public record Result(String blurb, int rounds, boolean accepted) {
	}

}
