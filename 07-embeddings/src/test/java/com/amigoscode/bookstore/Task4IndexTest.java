package com.amigoscode.bookstore;

import com.amigoscode.bookstore.search.CatalogIndexer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task4IndexTest {

	@Autowired
	JdbcClient jdbc;

	@Autowired
	CatalogIndexer indexer;

	@Test
	void everyBookIsInTheVectorStore() {
		assertThat(count()).isEqualTo(10);
	}

	@Test
	void eachRowHasTheTextTheEmbeddingAndTheMetadata() {
		var row = jdbc.sql("""
				select content, metadata::json->>'author' as author, metadata::json->>'year' as year,
				       vector_dims(embedding) as dims
				from vector_store where metadata::json->>'title' = 'Refactoring'""")
			.query((rs, i) -> rs.getString("content") + "|" + rs.getString("author") + "|" + rs.getString("year") + "|"
					+ rs.getInt("dims"))
			.single();

		assertThat(row).startsWith("Refactoring. A catalogue of small, safe steps").endsWith("|Martin Fowler|2018|384");
	}

	@Test
	void indexingAgainDoesNotCreateDuplicates() throws Exception {
		indexer.run(null);
		indexer.run(null);

		assertThat(count()).isEqualTo(10);
	}

	private long count() {
		return jdbc.sql("select count(*) from vector_store").query(Long.class).single();
	}

}
