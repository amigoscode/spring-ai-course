package com.amigoscode.bookstore;

import com.amigoscode.bookstore.search.Similarity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Plain maths, no Spring and no Docker needed.
 */
class Task2CosineTest {

	@Test
	void sameDirectionIsOne() {
		assertThat(Similarity.cosine(new float[] { 1, 0 }, new float[] { 1, 0 })).isCloseTo(1.0, within(1e-9));
		assertThat(Similarity.cosine(new float[] { 1, 2 }, new float[] { 2, 4 }))
			.as("length doesn't matter, only direction")
			.isCloseTo(1.0, within(1e-9));
	}

	@Test
	void unrelatedIsZero() {
		assertThat(Similarity.cosine(new float[] { 1, 0 }, new float[] { 0, 1 })).isCloseTo(0.0, within(1e-9));
	}

	@Test
	void oppositeIsMinusOne() {
		assertThat(Similarity.cosine(new float[] { 1, 0 }, new float[] { -1, 0 })).isCloseTo(-1.0, within(1e-9));
	}

	@Test
	void inBetween() {
		assertThat(Similarity.cosine(new float[] { 1, 1 }, new float[] { 1, 0 })).isCloseTo(Math.sqrt(0.5), within(1e-6));
	}

	@Test
	void vectorsMustHaveTheSameSize() {
		assertThatThrownBy(() -> Similarity.cosine(new float[] { 1 }, new float[] { 1, 2 }))
			.isInstanceOf(IllegalArgumentException.class);
	}

}
