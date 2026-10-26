package com.amigoscode.bookstore;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;

/**
 * A real MCP client, like the one inside Claude Desktop, connected to the app under test.
 */
final class McpTestClient {

	private McpTestClient() {
	}

	static McpSyncClient connect(int port) {
		var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port).build();
		McpSyncClient client = McpClient.sync(transport).build();
		client.initialize();
		return client;
	}

}
