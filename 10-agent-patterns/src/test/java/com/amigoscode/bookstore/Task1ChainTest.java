package com.amigoscode.bookstore;

import com.amigoscode.bookstore.agents.NewsletterChain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task1ChainTest {

	@Autowired
	NewsletterChain chain;

	@Autowired
	FakeChatModel model;

	@Test
	void eachStepFeedsTheNext() {
		model.reply("""
				["Clean Code", "Refactoring", "The Pragmatic Programmer"]""",
				"- Clean Code: readable code.\n- Refactoring: safe steps.\n- The Pragmatic Programmer: good habits.",
				"Hello readers! Here are this month's picks...");

		String newsletter = chain.write("craftsmanship");

		assertThat(newsletter).isEqualTo("Hello readers! Here are this month's picks...");
		var prompts = model.prompts();
		assertThat(prompts).hasSize(3);
		assertThat(prompts.get(0).getUserMessage().getText()).contains("craftsmanship").contains("Effective Java");
		assertThat(prompts.get(1).getUserMessage().getText()).contains("Clean Code").contains("Refactoring")
			.contains("The Pragmatic Programmer");
		assertThat(prompts.get(2).getUserMessage().getText()).contains("Refactoring: safe steps.");
	}

}
