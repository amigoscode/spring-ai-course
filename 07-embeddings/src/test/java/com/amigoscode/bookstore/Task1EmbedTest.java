package com.amigoscode.bookstore;

import com.amigoscode.bookstore.search.EmbeddingPlayground;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task1EmbedTest {

	@Autowired
	EmbeddingPlayground playground;

	@Autowired
	EmbeddingModel embeddingModel;

	@Test
	void textBecomesAVectorOf384Numbers() {
		float[] vector = playground.embed("A book about Java concurrency");

		assertThat(vector).hasSize(384);
		assertThat(embeddingModel.dimensions()).isEqualTo(384);
	}

	@Test
	void theSameTextAlwaysGivesTheSameVector() {
		assertThat(playground.embed("Clean Code")).containsExactly(playground.embed("Clean Code"));
	}

}
