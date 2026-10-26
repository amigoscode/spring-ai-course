package com.amigoscode.bookstore;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Conversations belong to the logged-in user. The id in the URL is only unique per user.
 */
@RestController
public class ConversationController {

	private final BookAssistant bookAssistant;

	private final ChatMemory chatMemory;

	public ConversationController(BookAssistant bookAssistant, ChatMemory chatMemory) {
		this.bookAssistant = bookAssistant;
		this.chatMemory = chatMemory;
	}

	@PostMapping("/api/conversations/{conversationId}")
	public String chat(Principal principal, @PathVariable String conversationId, @RequestBody String message) {
		return bookAssistant.chat(memoryKey(principal, conversationId), message);
	}

	@GetMapping("/api/conversations/{conversationId}")
	public List<HistoryEntry> history(Principal principal, @PathVariable String conversationId) {
		return chatMemory.get(memoryKey(principal, conversationId))
			.stream()
			.map(message -> new HistoryEntry(message.getMessageType().name(), message.getText()))
			.toList();
	}

	@DeleteMapping("/api/conversations/{conversationId}")
	public ResponseEntity<Void> forget(Principal principal, @PathVariable String conversationId) {
		chatMemory.clear(memoryKey(principal, conversationId));
		return ResponseEntity.noContent().build();
	}

	/**
	 * The user's own key for a conversation. Two users with the same conversation id get
	 * different keys, so nobody can read or delete someone else's chat.
	 */
	static String memoryKey(Principal principal, String conversationId) {
		return UUID.nameUUIDFromBytes(("chat:" + principal.getName() + ":" + conversationId)
			.getBytes(StandardCharsets.UTF_8)).toString();
	}

	public record HistoryEntry(String role, String text) {
	}

}
