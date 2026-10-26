package com.amigoscode.bookstore;

import java.util.List;

import com.amigoscode.bookstore.eval.FactCheck;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain Java, no Spring and no Docker needed.
 */
class Task1FactCheckTest {

	@Test
	void findsTheMissingFacts() {
		assertThat(FactCheck.missing("Express costs $9.90.", List.of("9.90", "2 business days")))
			.containsExactly("2 business days");
	}

	@Test
	void ignoresCase() {
		assertThat(FactCheck.missing("Returns are FREE in the EU", List.of("free"))).isEmpty();
	}

	@Test
	void treatsTypographicQuotesAsPlainOnes() {
		assertThat(FactCheck.missing("We don’t accept bank transfers.", List.of("don't"))).isEmpty();
	}

	@Test
	void ignoresExtraWhitespace() {
		assertThat(FactCheck.missing("You have 30\n   days to return it", List.of("30 days"))).isEmpty();
	}

	@Test
	void allFactsPresentMeansNothingMissing() {
		assertThat(FactCheck.missing("100 points give a $5 voucher", List.of("100", "$5"))).isEmpty();
	}

}
