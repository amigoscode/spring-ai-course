package com.amigoscode.bookstore;

import com.amigoscode.bookstore.policy.PolicyAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task2And3QuestionAnswerTest {

	@Autowired
	PolicyAssistant policyAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void task2_theRelevantPolicyIsInThePrompt() {
		model.reply("You have 30 days.");

		String answer = policyAssistant.answer("How long do I have to send a book back?");

		assertThat(answer).isEqualTo("You have 30 days.");
		String user = model.lastPrompt().getUserMessage().getText();
		assertThat(user).contains("How long do I have to send a book back?");
		assertThat(user).as("the retrieved context").contains("within 30 days of delivery");
	}

	@Test
	void task2_onlyPoliciesAreSearchedNotBooks() {
		model.reply("ok");

		policyAssistant.answer("Which book explains threads and locks in Java?");

		assertThat(model.lastPrompt().getUserMessage().getText()).doesNotContain("thread pools and the Java memory model");
	}

	@Test
	void task3_returnsTheSources() {
		model.reply("Every 100 points is a $5 voucher.");

		var result = policyAssistant.answerWithSources("What can I do with my loyalty points?");

		assertThat(result.answer()).isEqualTo("Every 100 points is a $5 voucher.");
		assertThat(result.sources()).contains("loyalty.md").doesNotHaveDuplicates();
	}

}
