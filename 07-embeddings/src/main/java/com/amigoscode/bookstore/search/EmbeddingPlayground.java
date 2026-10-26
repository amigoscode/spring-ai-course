package com.amigoscode.bookstore.search;

import java.util.Comparator;
import java.util.List;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

/**
 * Embeddings by hand, before the vector store does it for you.
 */
@Service
public class EmbeddingPlayground {

	private final EmbeddingModel embeddingModel;

	public EmbeddingPlayground(EmbeddingModel embeddingModel) {
		this.embeddingModel = embeddingModel;
	}

	/**
	 * Turns text into a vector of numbers that captures its meaning.
	 */
	public float[] embed(String text) {
		// SOLUTION-START task-1
		return embeddingModel.embed(text);
		// SOLUTION-ELSE
		// TASK: // TODO task-1: embed the text with the EmbeddingModel
		// TASK: throw new UnsupportedOperationException("TODO task-1");
		// SOLUTION-END
	}

	/**
	 * How similar two texts are in meaning, from -1 to 1.
	 */
	public double similarity(String a, String b) {
		return Similarity.cosine(embed(a), embed(b));
	}

	/**
	 * Ranks the candidates from most to least similar to the query. This is semantic
	 * search with no database: fine for 10 texts, far too slow for a million.
	 */
	public List<String> rank(String query, List<String> candidates) {
		// SOLUTION-START task-3
		float[] queryVector = embed(query);
		List<float[]> vectors = embeddingModel.embed(candidates);
		record Scored(String text, double score) {
		}
		return java.util.stream.IntStream.range(0, candidates.size())
			.mapToObj(i -> new Scored(candidates.get(i), Similarity.cosine(queryVector, vectors.get(i))))
			.sorted(Comparator.comparingDouble(Scored::score).reversed())
			.map(Scored::text)
			.toList();
		// SOLUTION-ELSE
		// TASK: // TODO task-3: embed the query and all candidates (in one batch call),
		// TASK: // then sort the candidates by cosine similarity to the query, highest first
		// TASK: throw new UnsupportedOperationException("TODO task-3");
		// SOLUTION-END
	}

}
