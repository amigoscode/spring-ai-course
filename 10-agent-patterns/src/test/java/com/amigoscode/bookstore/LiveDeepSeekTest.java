package com.amigoscode.bookstore;

import com.amigoscode.bookstore.agents.BlurbOptimizer;
import com.amigoscode.bookstore.agents.SupportRouter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Calls the real DeepSeek API. Costs a fraction of a cent.
 * Skipped by default. Run with: ./mvnw test -Plive
 */
@Tag("live")
@SpringBootTest
@Import(TestcontainersConfig.class)
class LiveDeepSeekTest {

	@Autowired
	SupportRouter router;

	@Autowired
	BlurbOptimizer optimizer;

	@Test
	void routesRealMessages() {
		assertThat(router.handle("How much is express shipping?").route()).isEqualTo(SupportRouter.Route.POLICY);
		assertThat(router.handle("Any good books about software architecture?").route())
			.isEqualTo(SupportRouter.Route.RECOMMENDATION);
		assertThat(router.handle("What's the capital of Peru?").route()).isEqualTo(SupportRouter.Route.OTHER);
	}

	@Test
	void writesABlurbThatPassesItsOwnReview() {
		var result = optimizer.write("Designing Data-Intensive Applications");

		assertThat(result.blurb()).contains("Designing Data-Intensive Applications");
		assertThat(result.rounds()).isBetween(1, 3);
	}

}
