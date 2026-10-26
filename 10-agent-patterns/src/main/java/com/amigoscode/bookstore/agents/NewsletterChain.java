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
		// TODO task-1: three calls, each using the previous result:
		//   1. pick 3 catalog titles for the theme, as a List<String>
		//   2. write a two sentence blurb for each title
		//   3. write the newsletter around those blurbs
		throw new UnsupportedOperationException("TODO task-1");
	}

}
