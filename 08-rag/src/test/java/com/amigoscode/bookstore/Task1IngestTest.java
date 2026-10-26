package com.amigoscode.bookstore;

import com.amigoscode.bookstore.policy.PolicyIngestor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task1IngestTest {

	@Autowired
	JdbcClient jdbc;

	@Autowired
	PolicyIngestor ingestor;

	@Test
	void everyPolicyFileIsChunkedAndStored() {
		var sources = jdbc.sql("select distinct metadata::json->>'source' from vector_store where metadata::json->>'type' = 'policy'")
			.query(String.class)
			.list();

		assertThat(sources).containsExactlyInAnyOrder("shipping.md", "returns.md", "payments.md", "loyalty.md");
		assertThat(policyChunks()).as("one chunk per section, at least").isGreaterThan(10);
	}

	@Test
	void theBooksAreStillThere() {
		long books = jdbc.sql("select count(*) from vector_store where metadata::json->>'type' = 'book'").query(Long.class).single();

		assertThat(books).isEqualTo(10);
	}

	@Test
	void ingestingAgainReplacesInsteadOfDuplicating() throws Exception {
		long before = policyChunks();

		ingestor.run(null);

		assertThat(policyChunks()).isEqualTo(before);
	}

	private long policyChunks() {
		return jdbc.sql("select count(*) from vector_store where metadata::json->>'type' = 'policy'").query(Long.class).single();
	}

}
