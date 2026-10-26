package com.amigoscode.bookstore.search;

/**
 * How close two embeddings are. Written by hand once, so you know what a vector store
 * does for you.
 */
public final class Similarity {

	private Similarity() {
	}

	/**
	 * Cosine similarity: 1 means the same direction (same meaning), 0 means unrelated.
	 * cos(a, b) = (a · b) / (|a| * |b|)
	 */
	public static double cosine(float[] a, float[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("Vectors have different sizes: " + a.length + " and " + b.length);
		}
		// TODO task-2: compute the dot product and both lengths in one loop, then divide
		throw new UnsupportedOperationException("TODO task-2");
	}

}
