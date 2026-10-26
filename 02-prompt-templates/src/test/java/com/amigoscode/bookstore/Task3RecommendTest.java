package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task3RecommendTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void rendersTheRecommendTemplate() {
		assertThat(new ClassPathResource("prompts/recommend.st").exists())
			.as("src/main/resources/prompts/recommend.st exists")
			.isTrue();

		bookAssistant.recommend("software testing", 2);

		String user = model.lastPrompt().getUserMessage().getText();
		assertThat(user).contains("software testing").contains("2");
		assertThat(user).as("every {placeholder} was filled in").doesNotContain("{").doesNotContain("}");
	}

}
