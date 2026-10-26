package com.amigoscode.bookstore;

import com.amigoscode.bookstore.advisor.TokenUsageTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task1TokenUsageTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Autowired
	TokenUsageTracker tracker;

	@BeforeEach
	void reset() {
		tracker.reset();
		model.reply("ok");
	}

	@Test
	void everyCallIsCounted() {
		bookAssistant.ask("Hi");
		bookAssistant.recommend("java", 2);

		assertThat(tracker.totals()).isEqualTo(new TokenUsageTracker.Totals(2,
				2 * FakeChatModel.PROMPT_TOKENS, 2 * FakeChatModel.COMPLETION_TOKENS));
	}

}
