package com.amigoscode.bookstore;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.amigoscode.bookstore.gifts.GiftFinder;
import com.amigoscode.bookstore.gifts.GiftRequest;
import com.amigoscode.bookstore.gifts.GiftSuggestions;
import com.amigoscode.bookstore.order.OrderService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The real acceptance test: the gift golden dataset against DeepSeek.
 * The rules must hold for every answer. The suggestions must be relevant for at least 80%.
 * Run with: ./mvnw test -Plive
 */
@Tag("live")
@SpringBootTest
@Import(TestcontainersConfig.class)
class LiveCapstoneTest {

	@Autowired
	GiftFinder giftFinder;

	@Autowired
	BookCatalog catalog;

	@Autowired
	OrderService orderService;

	@Test
	void giftsFollowTheRulesAndMakeSense() throws IOException {
		orderService.reset();
		List<GoldenGift> dataset = JsonMapper.builder()
			.build()
			.readValue(new ClassPathResource("eval/gift-golden.json").getInputStream(), new TypeReference<>() {
			});

		int relevant = 0;
		for (GoldenGift golden : dataset) {
			var request = new GiftRequest(golden.recipient(), golden.occasion(), golden.budget());
			GiftSuggestions result = giftFinder.find(request);

			GiftRules.check(result, request, catalog, orderService);
			var titles = result.suggestions().stream().map(GiftSuggestions.Suggestion::title).toList();
			boolean ok = titles.stream().anyMatch(golden.anyOf()::contains);
			relevant += ok ? 1 : 0;
			System.out.printf("%s  %-70s $%s -> %s ($%s)%n", ok ? "PASS" : "FAIL", golden.recipient(), golden.budget(),
					titles, result.total());
		}

		assertThat((double) relevant / dataset.size()).as("relevant suggestions").isGreaterThanOrEqualTo(0.8);
	}

	record GoldenGift(String recipient, String occasion, BigDecimal budget, List<String> anyOf) {
	}

}
