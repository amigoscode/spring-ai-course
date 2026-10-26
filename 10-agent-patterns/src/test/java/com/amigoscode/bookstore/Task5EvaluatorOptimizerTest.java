package com.amigoscode.bookstore;

import com.amigoscode.bookstore.agents.BlurbOptimizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task5EvaluatorOptimizerTest {

	@Autowired
	BlurbOptimizer optimizer;

	@Autowired
	FakeChatModel model;

	@Test
	void triesAgainWithTheFeedbackUntilItPasses() {
		model.reply("A must-read page-turner!", """
				{"verdict": "NEEDS_IMPROVEMENT", "feedback": "No title, and it's full of clichés"}""",
				"Clean Code teaches working developers to write code their team can read.", """
				{"verdict": "PASS", "feedback": "Good"}""");

		var result = optimizer.write("Clean Code");

		assertThat(result).isEqualTo(new BlurbOptimizer.Result(
				"Clean Code teaches working developers to write code their team can read.", 2, true));
		assertThat(model.prompts()).hasSize(4);
		assertThat(model.prompts().get(2).getUserMessage().getText()).as("the second draft sees the feedback")
			.contains("No title, and it's full of clichés")
			.contains("A must-read page-turner!");
	}

	@Test
	void stopsAfterTheLastRound() {
		model.reply("Draft", """
				{"verdict": "NEEDS_IMPROVEMENT", "feedback": "Still not good"}""", "Draft", """
				{"verdict": "NEEDS_IMPROVEMENT", "feedback": "Still not good"}""", "Draft", """
				{"verdict": "NEEDS_IMPROVEMENT", "feedback": "Still not good"}""", "Should never be asked");

		var result = optimizer.write("Refactoring");

		assertThat(result.accepted()).isFalse();
		assertThat(result.rounds()).isEqualTo(3);
		assertThat(model.prompts()).as("3 rounds of write and evaluate, then stop").hasSize(6);
	}

}
