package com.amigoscode.bookstore.search;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.amigoscode.bookstore.Book;
import com.amigoscode.bookstore.BookCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Puts every book of the catalog into the vector store when the app starts.
 */
@Component
public class CatalogIndexer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(CatalogIndexer.class);

	private final BookCatalog catalog;

	private final VectorStore vectorStore;

	public CatalogIndexer(BookCatalog catalog, VectorStore vectorStore) {
		this.catalog = catalog;
		this.vectorStore = vectorStore;
	}

	@Override
	public void run(ApplicationArguments args) {
		List<Document> documents = catalog.findAll().stream().map(CatalogIndexer::toDocument).toList();
		vectorStore.delete(documents.stream().map(Document::getId).toList());
		vectorStore.add(documents);
		log.info("Indexed {} books in the vector store", documents.size());
	}

	/**
	 * The text is what gets embedded and searched. The metadata is for filtering and display.
	 */
	static Document toDocument(Book book) {
		String text = book.title() + ". " + book.description();
		Map<String, Object> metadata = Map.of(
				"type", "book",
				"title", book.title(),
				"author", book.author(),
				"year", book.year(),
				"topics", String.join(", ", book.topics()));
		return new Document(idOf(book), text, metadata);
	}

	/**
	 * The same book always gets the same id, so indexing again replaces instead of duplicating.
	 */
	static String idOf(Book book) {
		return UUID.nameUUIDFromBytes(book.title().getBytes(StandardCharsets.UTF_8)).toString();
	}

}
