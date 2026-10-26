package com.amigoscode.bookstore.eval;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

public final class GoldenDataset {

	private GoldenDataset() {
	}

	public static List<GoldenQuestion> policies() {
		try (InputStream in = new ClassPathResource("eval/policy-golden.json").getInputStream()) {
			return JsonMapper.builder().build().readValue(in, new TypeReference<List<GoldenQuestion>>() {
			});
		}
		catch (IOException e) {
			throw new IllegalStateException("Could not read the golden dataset", e);
		}
	}

}
