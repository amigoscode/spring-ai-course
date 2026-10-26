package com.amigoscode.bookstore;

import com.amigoscode.bookstore.eval.EvalRunner;
import com.amigoscode.bookstore.eval.GoldenDataset;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The real evaluation: the whole golden dataset against DeepSeek. About 20 model calls,
 * still a fraction of a cent. Run with: ./mvnw test -Plive
 */
@Tag("live")
@SpringBootTest
@Import(TestcontainersConfig.class)
class LiveEvalTest {

	@Autowired
	EvalRunner runner;

	@Test
	void thePolicyAssistantPassesTheGoldenDataset() {
		var report = runner.run(GoldenDataset.policies());

		System.out.println(report.table());
		assertThat(report.passRate()).isGreaterThanOrEqualTo(0.8);
	}

}
