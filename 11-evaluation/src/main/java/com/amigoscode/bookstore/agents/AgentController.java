package com.amigoscode.bookstore.agents;

import java.util.List;
import java.util.Map;

import com.amigoscode.bookstore.Sentiment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

	private final NewsletterChain newsletterChain;

	private final SupportRouter supportRouter;

	private final ReviewAnalyzer reviewAnalyzer;

	private final ReadingPlanner readingPlanner;

	private final BlurbOptimizer blurbOptimizer;

	public AgentController(NewsletterChain newsletterChain, SupportRouter supportRouter, ReviewAnalyzer reviewAnalyzer,
			ReadingPlanner readingPlanner, BlurbOptimizer blurbOptimizer) {
		this.newsletterChain = newsletterChain;
		this.supportRouter = supportRouter;
		this.reviewAnalyzer = reviewAnalyzer;
		this.readingPlanner = readingPlanner;
		this.blurbOptimizer = blurbOptimizer;
	}

	@GetMapping("/newsletter")
	public String newsletter(@RequestParam String theme) {
		return newsletterChain.write(theme);
	}

	@PostMapping("/support")
	public SupportRouter.Routed support(@RequestBody String message) {
		return supportRouter.handle(message);
	}

	@PostMapping("/reviews")
	public Map<Sentiment, Long> reviews(@RequestBody List<String> reviews) {
		return reviewAnalyzer.summarize(reviews);
	}

	@GetMapping("/reading-plan")
	public String readingPlan(@RequestParam String goal) {
		return readingPlanner.plan(goal);
	}

	@GetMapping("/blurb")
	public BlurbOptimizer.Result blurb(@RequestParam String title) {
		return blurbOptimizer.write(title);
	}

}
