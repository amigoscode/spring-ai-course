package com.amigoscode.bookstore.policy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * The ETL pipeline: Extract the shop's policy documents, Transform them into small chunks,
 * Load them into the vector store.
 */
@Component
public class PolicyIngestor implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(PolicyIngestor.class);

	public static final String DOCS = "classpath:docs/*.md";

	private final VectorStore vectorStore;

	public PolicyIngestor(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	@Override
	public void run(ApplicationArguments args) throws IOException {
		List<Document> chunks = new ArrayList<>();
		for (Resource resource : new PathMatchingResourcePatternResolver().getResources(DOCS)) {
			chunks.addAll(read(resource));
		}
		// SOLUTION-START task-1
		vectorStore.delete("type == 'policy'");
		vectorStore.add(split(chunks));
		// SOLUTION-ELSE
		// TASK: // TODO task-1: replace the policy documents in the vector store with the new chunks.
		// TASK: // Delete the old ones by their "type" metadata, then add split(chunks).
		// SOLUTION-END
		log.info("Indexed {} policy chunks", chunks.size());
	}

	/**
	 * Extract: one Document per section of the Markdown file, with metadata saying what it is
	 * and where it came from.
	 */
	static List<Document> read(Resource resource) {
		// SOLUTION-START task-1
		var config = MarkdownDocumentReaderConfig.builder()
			.withAdditionalMetadata("type", "policy")
			.withAdditionalMetadata("source", resource.getFilename())
			.build();
		return new MarkdownDocumentReader(resource, config).get();
		// SOLUTION-ELSE
		// TASK: // TODO task-1: read the Markdown file with a MarkdownDocumentReader, adding the metadata
		// TASK: // "type" = "policy" and "source" = the file name to every document
		// TASK: return List.of();
		// SOLUTION-END
	}

	/**
	 * Transform: split long sections into chunks small enough to embed and to fit in a prompt.
	 */
	static List<Document> split(List<Document> documents) {
		return TokenTextSplitter.builder().withChunkSize(300).build().apply(documents);
	}

}
