package com.amigoscode.bookstore;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@AutoConfigureMockMvc
@Import(FakeChatModelConfig.class)
class Task4StreamingTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Autowired
	MockMvcTester mvc;

	@Test
	void streamsTheAnswerInPieces() {
		model.reply("We have three Java books in stock.");

		var pieces = bookAssistant.stream("Any Java books?").collectList().block(Duration.ofSeconds(5));

		assertThat(pieces).hasSizeGreaterThan(1);
		assertThat(String.join("", pieces)).isEqualTo("We have three Java books in stock.");
	}

	@Test
	void streamEndpointSendsServerSentEvents() {
		model.reply("We have three Java books.");

		assertThat(mvc.get().uri("/api/chat/stream").param("question", "Any Java books?").exchange())
			.hasStatusOk()
			.hasContentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
			.bodyText()
			.contains("data:We ")
			.contains("data:books.");
	}

}
