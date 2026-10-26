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
		// SOLUTION-START task-2
		double dot = 0;
		double normA = 0;
		double normB = 0;
		for (int i = 0; i < a.length; i++) {
			dot += a[i] * b[i];
			normA += a[i] * a[i];
			normB += b[i] * b[i];
		}
		return dot / (Math.sqrt(normA) * Math.sqrt(normB));
		// SOLUTION-ELSE
		// TASK: // TODO task-2: compute the dot product and both lengths in one loop, then divide
		// TASK: throw new UnsupportedOperationException("TODO task-2");
		// SOLUTION-END
	}

}
