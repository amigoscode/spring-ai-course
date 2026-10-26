package com.amigoscode.bookstore.policy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PolicyController {

	private final PolicyAssistant policyAssistant;

	public PolicyController(PolicyAssistant policyAssistant) {
		this.policyAssistant = policyAssistant;
	}

	@GetMapping("/api/help")
	public PolicyAssistant.AnswerWithSources help(@RequestParam String q) {
		return policyAssistant.answerWithSources(q);
	}

	@GetMapping("/api/help/advanced")
	public String helpAdvanced(@RequestParam String q) {
		return policyAssistant.answerAdvanced(q);
	}

}
