package com.amigoscode.bookstore;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task3PostgresMemoryTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Autowired
	JdbcClient jdbc;

	@Test
	void messagesAreSavedInPostgres() {
		String conversation = UUID.randomUUID().toString();
		model.reply("Hello!");

		bookAssistant.chat(conversation, "Hi there");

		var rows = jdbc.sql("""
				select type, content from spring_ai_chat_memory
				where conversation_id = ? order by sequence_id""")
			.param(conversation)
			.query((rs, i) -> rs.getString("type") + ": " + rs.getString("content"))
			.list();
		assertThat(rows).containsExactly("USER: Hi there", "ASSISTANT: Hello!");
	}

}
