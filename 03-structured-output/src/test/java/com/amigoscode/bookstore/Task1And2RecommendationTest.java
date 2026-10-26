package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task1And2RecommendationTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void task1_turnsTheJsonAnswerIntoARecord() {
		model.reply("""
				{"title": "Effective Java", "author": "Joshua Bloch", "reason": "The Java best practices book."}
				""");

		BookRecommendation book = bookAssistant.recommendOne("java");

		assertThat(book).isEqualTo(new BookRecommendation("Effective Java", "Joshua Bloch", "The Java best practices book."));
	}

	@Test
	void task1_tellsTheModelWhichJsonToReturn() {
		model.reply("""
				{"title": "Effective Java", "author": "Joshua Bloch", "reason": "x"}
				""");

		bookAssistant.recommendOne("java");

		String user = model.lastPrompt().getUserMessage().getText();
		assertThat(user).contains("java");
		assertThat(user).as("Spring AI adds the JSON schema to the prompt").contains("JSON").contains("\"reason\"");
	}

	@Test
	void task1_acceptsJsonWrappedInAMarkdownCodeBlock() {
		model.reply("""
				```json
				{"title": "Refactoring", "author": "Martin Fowler", "reason": "x"}
				```
				""");

		assertThat(bookAssistant.recommendOne("refactoring").title()).isEqualTo("Refactoring");
	}

	@Test
	void task2_returnsAList() {
		model.reply("""
				[
				  {"title": "Clean Code", "author": "Robert C. Martin", "reason": "a"},
				  {"title": "Refactoring", "author": "Martin Fowler", "reason": "b"}
				]
				""");

		var books = bookAssistant.recommendMany("craftsmanship", 2);

		assertThat(books).extracting(BookRecommendation::title).containsExactly("Clean Code", "Refactoring");
		assertThat(model.lastPrompt().getUserMessage().getText()).contains("craftsmanship").contains("2");
	}

}
