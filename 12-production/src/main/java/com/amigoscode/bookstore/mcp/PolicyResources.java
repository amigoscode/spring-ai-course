package com.amigoscode.bookstore.mcp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * The shop's policy documents, published as MCP resources. A client (like Claude Desktop)
 * can attach them to a conversation, instead of the model having to call a tool.
 */
@Component
public class PolicyResources {

	@McpResource(uri = "policy://{name}", name = "Shop policy",
			description = "One of the Amigos Books policies: shipping, returns, payments or loyalty",
			mimeType = "text/markdown")
	public String policy(String name) throws IOException {
		var file = new ClassPathResource("docs/" + name.replaceAll("[^a-z]", "") + ".md");
		if (!file.exists()) {
			throw new IllegalArgumentException("No policy called " + name);
		}
		return file.getContentAsString(StandardCharsets.UTF_8);
	}

}
