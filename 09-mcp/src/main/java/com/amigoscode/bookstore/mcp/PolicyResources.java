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

	// TODO task-3: publish each docs/<name>.md file as the MCP resource policy://<name>
	// with @McpResource. Only allow letters in the name: it ends up in a file path.

}
