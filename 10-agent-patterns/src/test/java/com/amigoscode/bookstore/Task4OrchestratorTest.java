package com.amigoscode.bookstore;

import com.amigoscode.bookstore.agents.ReadingPlanner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task4OrchestratorTest {

	@Autowired
	ReadingPlanner planner;

	@Autowired
	FakeChatModel model;

	@Test
	void oneWorkerPerPhaseThenOneSynthesis() {
		model.reply("""
				{"phases": [
				  {"title": "Foundations", "focus": "Java and clean code"},
				  {"title": "Design", "focus": "patterns and architecture"},
				  {"title": "Scale", "focus": "distributed systems"}
				]}""", "Worker answer", "Worker answer", "Worker answer", "The final plan");

		String plan = planner.plan("become a senior backend engineer");

		assertThat(plan).isEqualTo("The final plan");
		var prompts = model.prompts();
		assertThat(prompts).as("1 plan + 3 workers + 1 synthesis").hasSize(5);
		assertThat(prompts.get(0).getUserMessage().getText()).contains("become a senior backend engineer");
		assertThat(prompts.get(1).getUserMessage().getText()).contains("Foundations").contains("Effective Java");
		assertThat(prompts.get(3).getUserMessage().getText()).contains("distributed systems");
		assertThat(prompts.get(4).getUserMessage().getText()).contains("Worker answer");
	}

	@Test
	void neverRunsMoreThanFourWorkers() {
		model.reply("""
				{"phases": [
				  {"title": "1", "focus": "a"}, {"title": "2", "focus": "b"}, {"title": "3", "focus": "c"},
				  {"title": "4", "focus": "d"}, {"title": "5", "focus": "e"}, {"title": "6", "focus": "f"}
				]}""", "Worker answer");

		planner.plan("learn everything");

		assertThat(model.prompts()).as("1 plan + at most 4 workers + 1 synthesis").hasSize(6);
	}

}
