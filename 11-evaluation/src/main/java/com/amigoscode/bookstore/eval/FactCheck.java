package com.amigoscode.bookstore.eval;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Cheap, deterministic checks on an answer. Run these before any LLM judge: they're free,
 * fast and never flaky.
 */
public final class FactCheck {

	private FactCheck() {
	}

	/**
	 * The facts from the list that the answer does NOT mention. Empty means all good.
	 * Model answers vary in case, spacing and punctuation ("Don’t" vs "don't"), so compare
	 * normalised text.
	 */
	public static List<String> missing(String answer, List<String> mustMention) {
		// TODO task-1: return the facts that aren't in the answer, comparing normalise(...)d text
		throw new UnsupportedOperationException("TODO task-1");
	}

	/**
	 * Lower case, typographic quotes and dashes replaced by plain ones, whitespace collapsed.
	 */
	static String normalise(String text) {
		// TODO task-1: lower case, replace ’ ‘ with ' and “ ” with ", replace – — with -,
		// and collapse runs of whitespace into one space
		return text;
	}

}
