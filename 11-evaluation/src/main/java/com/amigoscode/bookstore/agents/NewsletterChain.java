package com.amigoscode.bookstore.agents;

import java.util.List;

import com.amigoscode.bookstore.BookCatalog;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * Pattern 1: prompt chaining. A big task split into fixed steps, where each step's output
 * is the next step's input. Each prompt is small and easy to get right.
 *
 * pick books ──► write a blurb for each ──► assemble the newsletter
 */
@Service
public class NewsletterChain {

	private final ChatClient chatClient;

	private final BookCatalog catalog;

	public NewsletterChain(ChatClient.Builder chatClientBuilder, BookCatalog catalog) {
		this.chatClient = chatClientBuilder.clone().build();
		this.catalog = catalog;
	}

	public String write(String theme) {
		List<String> titles = chatClient.prompt()
			.user(u -> u.text("""
					Pick exactly 3 books for a newsletter with the theme "{theme}".
					Only use titles from this catalog:
					{catalog}""").param("theme", theme).param("catalog", catalog.asPromptText()))
			.call()
			.entity(new ParameterizedTypeReference<List<String>>() {
			});

		String blurbs = chatClient.prompt()
			.user(u -> u.text("Write a two sentence blurb for each of these books, as a bullet list:\n{titles}")
				.param("titles", String.join("\n", titles)))
			.call()
			.content();

		return chatClient.prompt()
			.user(u -> u.text("""
					Write a short, friendly newsletter for Amigos Books with the theme "{theme}".
					Start with a one line greeting, then include these blurbs unchanged:
					{blurbs}""").param("theme", theme).param("blurbs", blurbs))
			.call()
			.content();
	}

}
