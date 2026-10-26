package com.amigoscode.bookstore;

import java.util.Map;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task1And2McpServerTest {

	@LocalServerPort
	int port;

	McpSyncClient client;

	@BeforeEach
	void connect() {
		client = McpTestClient.connect(port);
	}

	@AfterEach
	void close() {
		client.closeGracefully();
	}

	@Test
	void task1_onlyTheReadOnlyToolsArePublished() {
		var names = client.listTools().tools().stream().map(McpSchema.Tool::name).toList();

		assertThat(names).containsExactlyInAnyOrder("checkStock", "findBooksByTopic", "searchShopPolicies");
		assertThat(names).doesNotContain("placeOrder", "orderStatus");
	}

	@Test
	void task1_aClientCanCallATool() {
		var result = client.callTool(McpSchema.CallToolRequest.builder("checkStock")
			.arguments(Map.of("title", "Clean Code"))
			.build());

		assertThat(result.isError()).isFalse();
		assertThat(((McpSchema.TextContent) result.content().getFirst()).text()).contains("copies").contains("3");
	}

	@Test
	void task2_theServerIntroducesItself() {
		var init = client.getCurrentInitializationResult();

		assertThat(init.serverInfo().name()).isEqualTo("amigos-books");
		assertThat(init.serverInfo().version()).isEqualTo("1.0.0");
		assertThat(init.instructions()).contains("Amigos Books");
	}

}
