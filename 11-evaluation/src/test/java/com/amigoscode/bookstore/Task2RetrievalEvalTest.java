package com.amigoscode.bookstore;

import com.amigoscode.bookstore.eval.GoldenDataset;
import com.amigoscode.bookstore.eval.RetrievalEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task2RetrievalEvalTest {

	@Autowired
	RetrievalEvaluator evaluator;

	@Test
	void retrievalFindsTheRightDocumentForMostQuestions() {
		var dataset = GoldenDataset.policies();

		var report = evaluator.evaluate(dataset, 4);

		System.out.printf("hit rate %.2f, MRR %.2f, misses %s%n", report.hitRate(), report.mrr(), report.misses());
		assertThat(report.hitRate()).isGreaterThanOrEqualTo(0.8);
		assertThat(report.mrr()).isPositive().isLessThanOrEqualTo(report.hitRate());
		assertThat(report.misses()).hasSize((int) Math.round(dataset.size() * (1 - report.hitRate())));
	}

	@Test
	void fewerResultsCanOnlyFindLess() {
		var dataset = GoldenDataset.policies();

		assertThat(evaluator.evaluate(dataset, 1).hitRate()).isLessThanOrEqualTo(evaluator.evaluate(dataset, 4).hitRate());
	}

}
