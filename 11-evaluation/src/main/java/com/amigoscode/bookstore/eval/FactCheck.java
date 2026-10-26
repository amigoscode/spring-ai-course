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
		// SOLUTION-START task-1
		String normalisedAnswer = normalise(answer);
		return mustMention.stream().filter(fact -> !normalisedAnswer.contains(normalise(fact))).toList();
		// SOLUTION-ELSE
		// TASK: // TODO task-1: return the facts that aren't in the answer, comparing normalise(...)d text
		// TASK: throw new UnsupportedOperationException("TODO task-1");
		// SOLUTION-END
	}

	/**
	 * Lower case, typographic quotes and dashes replaced by plain ones, whitespace collapsed.
	 */
	static String normalise(String text) {
		// SOLUTION-START task-1
		return Normalizer.normalize(text, Normalizer.Form.NFKC)
			.toLowerCase(Locale.ROOT)
			.replace('’', '\'')
			.replace('‘', '\'')
			.replace('“', '"')
			.replace('”', '"')
			.replace('–', '-')
			.replace('—', '-')
			.replaceAll("\\s+", " ")
			.strip();
		// SOLUTION-ELSE
		// TASK: // TODO task-1: lower case, replace ’ ‘ with ' and “ ” with ", replace – — with -,
		// TASK: // and collapse runs of whitespace into one space
		// TASK: return text;
		// SOLUTION-END
	}

}
