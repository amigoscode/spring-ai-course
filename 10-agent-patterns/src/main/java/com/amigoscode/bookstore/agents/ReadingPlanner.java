package com.amigoscode.bookstore.agents;

import java.util.List;
import java.util.stream.Collectors;

import com.amigoscode.bookstore.BookCatalog;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Pattern 4: orchestrator-workers. Unlike a chain, the steps aren't known in advance: an
 * orchestrator call decides them, a worker call handles each one, and a final call puts it together.
 *
 *                ┌─► worker: phase 1 ─┐
 * goal ─► plan ──┼─► worker: phase 2 ─┼─► synthesize ─► reading plan
 *                └─► worker: phase 3 ─┘
 */
@Service
public class ReadingPlanner {

	private final ChatClient chatClient;

	private final BookCatalog catalog;

	public ReadingPlanner(ChatClient.Builder chatClientBuilder, BookCatalog catalog) {
		this.chatClient = chatClientBuilder.clone().build();
		this.catalog = catalog;
	}

	public String plan(String goal) {
		// SOLUTION-START task-4
		Plan plan = chatClient.prompt()
			.user(u -> u.text("""
					A customer has this learning goal: "{goal}".
					Split it into 2 to 4 learning phases, from beginner to advanced.""").param("goal", goal))
			.call()
			.entity(Plan.class);

		List<String> sections = plan.phases()
			.stream()
			.map(phase -> chatClient.prompt()
				.user(u -> u.text("""
						Learning phase: {phase}
						Pick the best 1 or 2 books for it from the catalog and say in one sentence why.
						Catalog:
						{catalog}""").param("phase", phase.title() + ": " + phase.focus())
					.param("catalog", catalog.asPromptText()))
				.call()
				.content())
			.toList();

		return chatClient.prompt()
			.user(u -> u.text("""
					Write a reading plan for the goal "{goal}" from these phases, in order.
					Keep every book recommendation.
					{sections}""").param("goal", goal)
				.param("sections", sections.stream().collect(Collectors.joining("\n\n"))))
			.call()
			.content();
		// SOLUTION-ELSE
		// TASK: // TODO task-4: the orchestrator splits the goal into a Plan of phases (structured output),
		// TASK: // one worker call per phase picks books from the catalog,
		// TASK: // and a last call combines the sections into a reading plan
		// TASK: throw new UnsupportedOperationException("TODO task-4");
		// SOLUTION-END
	}

	public record Plan(List<Phase> phases) {
	}

	public record Phase(String title, String focus) {
	}

}
