package com.amigoscode.bookstore;

import com.amigoscode.bookstore.policy.PolicyAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task5CacheTest {

	@Autowired
	PolicyAssistant policyAssistant;

	@Autowired
	FakeChatModel model;

	@Autowired
	CacheManager cacheManager;

	@BeforeEach
	void clearCaches() {
		cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
	}

	@Test
	void theSameQuestionIsAnsweredOnce() {
		model.reply("You have 30 days.");

		var first = policyAssistant.answerWithSources("How long do I have to return a book?");
		var second = policyAssistant.answerWithSources("  how long do I have to RETURN a book? ");

		assertThat(second).isEqualTo(first);
		assertThat(model.prompts()).as("the second answer came from the cache").hasSize(1);
	}

	@Test
	void differentQuestionsAreNotMixedUp() {
		model.reply("An answer");

		policyAssistant.answerWithSources("How long do I have to return a book?");
		policyAssistant.answerWithSources("How much is express delivery?");

		assertThat(model.prompts()).hasSize(2);
	}

}
