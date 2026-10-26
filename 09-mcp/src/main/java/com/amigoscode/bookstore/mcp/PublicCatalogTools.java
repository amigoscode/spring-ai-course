package com.amigoscode.bookstore.mcp;

import java.util.List;

import com.amigoscode.bookstore.Book;
import com.amigoscode.bookstore.tools.BookstoreTools;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpTool.McpAnnotations;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * What the outside world can do through MCP. An explicit allow-list: only read-only tools.
 * placeOrder and orderStatus act for a logged-in customer, and an MCP client has no
 * logged-in customer, so they are deliberately not here.
 */
@Component
public class PublicCatalogTools {

	private final BookstoreTools tools;

	public PublicCatalogTools(BookstoreTools tools) {
		this.tools = tools;
	}

	// TODO task-1: publish checkStock, findBooksByTopic and searchShopPolicies as MCP tools
	// with @McpTool and @McpToolParam. Each one calls the same method on BookstoreTools.
	// Mark them read-only with @McpAnnotations(readOnlyHint = true, destructiveHint = false).

}
