package com.amigoscode.bookstore;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Import this in a test to swap DeepSeek for {@link FakeChatModel}.
 * Pair it with the property spring.ai.model.chat=none so DeepSeek is switched off.
 */
@TestConfiguration(proxyBeanMethods = false)
public class FakeChatModelConfig {

	@Bean
	FakeChatModel fakeChatModel() {
		return new FakeChatModel();
	}

}
