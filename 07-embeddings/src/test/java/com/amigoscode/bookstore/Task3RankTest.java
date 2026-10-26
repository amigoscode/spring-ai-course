package com.amigoscode.bookstore;

import java.util.List;

import com.amigoscode.bookstore.search.EmbeddingPlayground;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task3RankTest {

	@Autowired
	EmbeddingPlayground playground;

	@Autowired
	BookCatalog catalog;

	@Test
	void similarMeaningScoresHigherThanSharedWords() {
		double sameMeaning = playground.similarity("cheap flights to Lisbon", "low cost plane tickets to Portugal");
		double unrelated = playground.similarity("cheap flights to Lisbon", "generics and type erasure in Java");

		assertThat(sameMeaning).isGreaterThan(unrelated);
	}

	@Test
	void findsTheRightBookByMeaning() {
		List<String> descriptions = catalog.findAll().stream().map(Book::description).toList();

		List<String> ranked = playground.rank("write a failing unit test before the implementation", descriptions);

		assertThat(ranked).hasSameSizeAs(descriptions);
		assertThat(ranked.getFirst()).isEqualTo(catalog.findByTitle("Test Driven Development: By Example").orElseThrow().description());
	}

}
