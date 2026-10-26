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
		List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
			.query(query)
			.topK(topK)
			.similarityThreshold(MIN_SIMILARITY)
			.filterExpression("type == 'book'")
			.build());
		return documents.stream().map(BookSearch::toResult).toList();
	}

	/**
	 * Same as {@link #search}, but only books published in or after the given year.
	 */
	public List<Result> searchPublishedSince(String query, int year, int topK) {
		List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
			.query(query)
			.topK(topK)
			.similarityThreshold(MIN_SIMILARITY)
			.filterExpression("type == 'book' && year >= " + year)
			.build());
		return documents.stream().map(BookSearch::toResult).toList();
	}

	private static Result toResult(Document document) {
		return new Result((String) document.getMetadata().get("title"), (String) document.getMetadata().get("author"),
				document.getScore());
	}

	public record Result(String title, String author, Double score) {
	}

}
