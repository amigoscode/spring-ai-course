package com.amigoscode.bookstore.eval;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * Is RAG finding the right documents? This needs no LLM at all: if retrieval misses, the
 * best model in the world can't answer, so measure it on its own.
 */
@Service
public class RetrievalEvaluator {

	private final VectorStore vectorStore;

	public RetrievalEvaluator(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	/**
	 * hit rate: share of questions whose expected source is in the top k results.
	 * MRR (mean reciprocal rank): average of 1/rank of the expected source (1 if it's first,
	 * 1/2 if second, ... and 0 if missing). It rewards finding it high up.
	 */
	public RetrievalReport evaluate(List<GoldenQuestion> dataset, int k) {
		// TODO task-2: for each question, search the policies (topK = k) and find the rank of the
		// expected source with rankOf. Return the hit rate, the MRR and the questions that missed.
		throw new UnsupportedOperationException("TODO task-2");
	}

	/**
	 * 1 if the first result comes from the source, 2 if the second... 0 if none do.
	 */
	static int rankOf(String source, List<Document> results) {
		for (int i = 0; i < results.size(); i++) {
			if (source.equals(results.get(i).getMetadata().get("source"))) {
				return i + 1;
			}
		}
		return 0;
	}

	public record RetrievalReport(double hitRate, double mrr, List<String> misses) {
	}

}
