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

	@McpTool(name = "checkStock", description = "Check how many copies of a book Amigos Books has in stock",
			annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
	public BookstoreTools.StockInfo checkStock(@McpToolParam(description = "The exact title of the book") String title) {
		return tools.checkStock(title);
	}

	@McpTool(name = "findBooksByTopic", description = "Find the books Amigos Books sells about a topic, such as java or testing",
			annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
	public List<Book> findBooksByTopic(@McpToolParam(description = "One topic, in lower case") String topic) {
		return tools.findBooksByTopic(topic);
	}

	@McpTool(name = "searchShopPolicies", description = "Search the Amigos Books policies about shipping, returns, payments and loyalty points",
			annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
	public List<String> searchShopPolicies(@McpToolParam(description = "The question, as a search query") String question) {
		return tools.searchShopPolicies(question);
	}

}
