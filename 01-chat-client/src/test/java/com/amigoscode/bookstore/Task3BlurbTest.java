package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task3BlurbTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void blurbUsesAHigherTemperatureForThisRequestOnly() {
		bookAssistant.writeBlurb("Clean Code");

		assertThat(model.lastPrompt().getUserMessage().getText()).contains("Clean Code");
		assertThat(model.lastPrompt().getOptions().getTemperature()).isEqualTo(1.2);

		bookAssistant.ask("Hi");
		assertThat(model.lastPrompt().getOptions().getTemperature())
			.as("other requests keep the default temperature")
			.isEqualTo(0.3);
	}

}
