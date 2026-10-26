package com.amigoscode.bookstore;

import com.amigoscode.bookstore.search.BookSearch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task5And6SearchTest {

	@Autowired
	BookSearch bookSearch;

	@Autowired
	BookCatalog catalog;

	@Test
	void task5_findsBooksByMeaning() {
		assertThat(bookSearch.search("write the test before the code", 1)).extracting(BookSearch.Result::title)
			.containsExactly("Test Driven Development: By Example");
		assertThat(bookSearch.search("running code safely on many threads", 1)).extracting(BookSearch.Result::title)
			.containsExactly("Java Concurrency in Practice");
	}

	@Test
	void task5_returnsAtMostTopKResultsWithScores() {
		var results = bookSearch.search("java", 3);

		assertThat(results).hasSizeLessThanOrEqualTo(3).isNotEmpty();
		assertThat(results).allSatisfy(r -> assertThat(r.score()).isBetween(0.3, 1.0));
	}

	@Test
	void task5_somethingUnrelatedFindsNothing() {
		assertThat(bookSearch.search("chocolate cake recipe with strawberries", 3)).isEmpty();
	}

	@Test
	void task6_filtersOnMetadata() {
		var results = bookSearch.searchPublishedSince("how to design good software", 2015, 10);

		assertThat(results).isNotEmpty();
		assertThat(results).allSatisfy(r -> assertThat(catalog.findByTitle(r.title()).orElseThrow().year()).isGreaterThanOrEqualTo(2015));
	}

}
