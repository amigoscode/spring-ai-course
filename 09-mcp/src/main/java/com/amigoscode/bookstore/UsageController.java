package com.amigoscode.bookstore;

import com.amigoscode.bookstore.advisor.TokenUsageTracker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsageController {

	private final TokenUsageTracker tracker;

	public UsageController(TokenUsageTracker tracker) {
		this.tracker = tracker;
	}

	@GetMapping("/api/usage")
	public TokenUsageTracker.Totals usage() {
		return tracker.totals();
	}

}
