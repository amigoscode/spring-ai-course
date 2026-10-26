package com.amigoscode.bookstore.search;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * Semantic search over the catalog: find books by what they're about, not by keywords.
 */
@Service
public class BookSearch {

	static final double MIN_SIMILARITY = 0.3;

	private final VectorStore vectorStore;

	public BookSearch(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	public List<Result> search(String query, int topK) {
		// TODO task-5: search the vector store for the topK most similar books,
		// ignoring anything below MIN_SIMILARITY
		throw new UnsupportedOperationException("TODO task-5");
	}

	/**
	 * Same as {@link #search}, but only books published in or after the given year.
	 */
	public List<Result> searchPublishedSince(String query, int year, int topK) {
		// TODO task-6: like search, plus a metadata filter on the year
		throw new UnsupportedOperationException("TODO task-6");
	}

	private static Result toResult(Document document) {
		return new Result((String) document.getMetadata().get("title"), (String) document.getMetadata().get("author"),
				document.getScore());
	}

	public record Result(String title, String author, Double score) {
	}

}
