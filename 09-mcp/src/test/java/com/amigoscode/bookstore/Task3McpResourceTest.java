package com.amigoscode.bookstore;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task3McpResourceTest {

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
	void policiesArePublishedAsAResourceTemplate() {
		var templates = client.listResourceTemplates().resourceTemplates();

		assertThat(templates).extracting(McpSchema.ResourceTemplate::uriTemplate).contains("policy://{name}");
	}

	@Test
	void aClientCanReadAPolicy() {
		var result = client.readResource(McpSchema.ReadResourceRequest.builder("policy://returns").build());

		var contents = (McpSchema.TextResourceContents) result.contents().getFirst();
		assertThat(contents.text()).contains("within 30 days of delivery");
	}

	@Test
	void theNameCannotEscapeTheDocsFolder() {
		assertThatThrownBy(() -> client.readResource(McpSchema.ReadResourceRequest.builder("policy://..%2F..%2Fapplication").build()))
			.isInstanceOf(RuntimeException.class);
	}

}
