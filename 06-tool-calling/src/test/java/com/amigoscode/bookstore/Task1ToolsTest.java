package com.amigoscode.bookstore;

import java.util.Arrays;

import com.amigoscode.bookstore.tools.BookstoreTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task1ToolsTest {

	@Autowired
	BookstoreTools tools;

	@Test
	void checkStockAndFindBooksByTopicAreTools() {
		var definitions = Arrays.stream(ToolCallbacks.from(tools)).map(ToolCallback::getToolDefinition).toList();

		assertThat(definitions).extracting(d -> d.name()).contains("checkStock", "findBooksByTopic");
		assertThat(definitions).allSatisfy(d -> assertThat(d.description()).as("every tool has a description").isNotBlank());
	}

	@Test
	void theModelSeesTheParametersAsAJsonSchema() {
		var checkStock = Arrays.stream(ToolCallbacks.from(tools))
			.map(ToolCallback::getToolDefinition)
			.filter(d -> d.name().equals("checkStock"))
			.findFirst()
			.orElseThrow();

		assertThat(checkStock.inputSchema()).contains("\"title\"").contains("\"type\" : \"string\"");
	}

	@Test
	void checkStockReturnsTheCopies() {
		var stock = tools.checkStock("Clean Code");

		assertThat(stock).isEqualTo(new BookstoreTools.StockInfo("Clean Code", true, 3));
		assertThat(tools.checkStock("Java Concurrency in Practice").inStock()).isFalse();
	}

	@Test
	void findBooksByTopic() {
		assertThat(tools.findBooksByTopic("testing")).extracting(Book::title)
			.containsExactly("Test Driven Development: By Example");
	}

}
