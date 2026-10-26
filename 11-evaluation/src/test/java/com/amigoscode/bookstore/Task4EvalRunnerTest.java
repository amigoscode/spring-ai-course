package com.amigoscode.bookstore;

import java.util.List;

import com.amigoscode.bookstore.eval.EvalRunner;
import com.amigoscode.bookstore.eval.GoldenQuestion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task4EvalRunnerTest {

	static final GoldenQuestion RETURNS = new GoldenQuestion("How long do I have to return a printed book?", "returns.md",
			List.of("30 days"));

	static final String GOOD_JUDGEMENT = """
			{"score": 5, "grounded": true, "reasoning": "Correct"}""";

	@Autowired
	EvalRunner runner;

	@Autowired
	FakeChatModel model;

	@Test
	void aGoodAnswerPassesAllThreeChecks() {
		model.reply("You have 30 days after delivery.", GOOD_JUDGEMENT);

		var report = runner.run(List.of(RETURNS));

		var result = report.results().getFirst();
		assertThat(result.missingFacts()).isEmpty();
		assertThat(result.rightSource()).isTrue();
		assertThat(result.judgement().score()).isEqualTo(5);
		assertThat(report.passRate()).isEqualTo(1.0);
	}

	@Test
	void aWrongFactFailsEvenIfTheJudgeLikesIt() {
		model.reply("You have 14 days after delivery.", GOOD_JUDGEMENT);

		var report = runner.run(List.of(RETURNS));

		assertThat(report.results().getFirst().missingFacts()).containsExactly("30 days");
		assertThat(report.passRate()).isZero();
	}

	@Test
	void theJudgeSeesTheRetrievedContext() {
		model.reply("You have 30 days.", GOOD_JUDGEMENT);

		runner.run(List.of(RETURNS));

		assertThat(model.lastPrompt().getUserMessage().getText()).contains("within 30 days of delivery");
	}

}
