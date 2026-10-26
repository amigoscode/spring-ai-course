package com.amigoscode.bookstore.mcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bonus: the other direction. Here the app is the MCP client, and DeepWiki's MCP server
 * provides the tools. Only active with the deepwiki profile.
 */
@Service
@RestController
@Profile("deepwiki")
public class RepoExpert {

	private final ChatClient chatClient;

	private final SyncMcpToolCallbackProvider deepWikiTools;

	public RepoExpert(ChatClient.Builder chatClientBuilder, SyncMcpToolCallbackProvider deepWikiTools) {
		this.chatClient = chatClientBuilder
			.defaultSystem("You answer questions about open source GitHub repositories. Use the tools to look them up.")
			.build();
		this.deepWikiTools = deepWikiTools;
	}

	@GetMapping("/api/repo-expert")
	public String ask(@RequestParam String q) {
		return chatClient.prompt().user(q).toolCallbacks(deepWikiTools).call().content();
	}

}
