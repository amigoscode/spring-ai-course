package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(ChatController.class)
class Task5ChatEndpointTest {

	@Autowired
	MockMvcTester mvc;

	@MockitoBean
	BookAssistant bookAssistant;

	@Test
	void returnsTheAnswerAsJson() {
		given(bookAssistant.askWithUsage("Any Java books?"))
			.willReturn(new BookAssistant.Answer("Plenty!", 10, 2));

		assertThat(mvc.get().uri("/api/chat").param("question", "Any Java books?"))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("""
					{"text": "Plenty!", "promptTokens": 10, "completionTokens": 2}
					""");
	}

}
