package com.amigoscode.bookstore;

import java.util.List;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConversationController {

	private final BookAssistant bookAssistant;

	private final ChatMemory chatMemory;

	public ConversationController(BookAssistant bookAssistant, ChatMemory chatMemory) {
		this.bookAssistant = bookAssistant;
		this.chatMemory = chatMemory;
	}

	@PostMapping("/api/conversations/{conversationId}")
	public String chat(@PathVariable String conversationId, @RequestBody String message) {
		return bookAssistant.chat(conversationId, message);
	}

	// TODO task-4: GET /api/conversations/{conversationId} returns the history as a list of HistoryEntry
	// TODO task-4: DELETE /api/conversations/{conversationId} forgets the conversation (204 No Content)

	public record HistoryEntry(String role, String text) {
	}

}
