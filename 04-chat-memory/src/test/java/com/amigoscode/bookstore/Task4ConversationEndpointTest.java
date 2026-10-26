package com.amigoscode.bookstore;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@AutoConfigureMockMvc
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task4ConversationEndpointTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	FakeChatModel model;

	@Test
	void showsAndForgetsTheHistory() {
		String id = UUID.randomUUID().toString();
		model.reply("Hello Ana!");
		assertThat(mvc.post().uri("/api/conversations/{id}", id).content("I'm Ana")).hasStatusOk();

		assertThat(mvc.get().uri("/api/conversations/{id}", id))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("""
					[{"role": "USER", "text": "I'm Ana"}, {"role": "ASSISTANT", "text": "Hello Ana!"}]
					""");

		assertThat(mvc.delete().uri("/api/conversations/{id}", id)).hasStatus(HttpStatus.NO_CONTENT);

		assertThat(mvc.get().uri("/api/conversations/{id}", id)).hasStatusOk().bodyJson().isEqualTo("[]");
	}

}
