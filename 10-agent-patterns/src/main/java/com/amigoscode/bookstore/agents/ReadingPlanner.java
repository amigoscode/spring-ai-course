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

	/**
	 * The model decides how many workers run. Cap it, so a strange plan can't start 50 calls.
	 */
	static final int MAX_PHASES = 4;

	private final ChatClient chatClient;

	private final BookCatalog catalog;

	public ReadingPlanner(ChatClient.Builder chatClientBuilder, BookCatalog catalog) {
		this.chatClient = chatClientBuilder.clone().build();
		this.catalog = catalog;
	}

	public String plan(String goal) {
		// TODO task-4: the orchestrator splits the goal into a Plan of phases (structured output),
		// one worker call per phase (at most MAX_PHASES) picks books from the catalog,
		// and a last call combines the sections into a reading plan
		throw new UnsupportedOperationException("TODO task-4");
	}

	public record Plan(List<Phase> phases) {
	}

	public record Phase(String title, String focus) {
	}

}
