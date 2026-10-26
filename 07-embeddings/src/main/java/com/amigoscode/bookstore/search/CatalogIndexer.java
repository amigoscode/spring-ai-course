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
		// TODO task-4: store the documents in the vector store. Running this twice
		// must not create duplicates: delete the documents with the same ids first.
		log.info("Indexed {} books in the vector store", documents.size());
	}

	/**
	 * The text is what gets embedded and searched. The metadata is for filtering and display.
	 */
	static Document toDocument(Book book) {
		// TODO task-4: a Document with the id idOf(book), the title and description as text,
		// and the title, author, year and topics (joined with ", ") as metadata
		return new Document(idOf(book), book.title(), Map.of());
	}

	/**
	 * The same book always gets the same id, so indexing again replaces instead of duplicating.
	 */
	static String idOf(Book book) {
		return UUID.nameUUIDFromBytes(book.title().getBytes(StandardCharsets.UTF_8)).toString();
	}

}
