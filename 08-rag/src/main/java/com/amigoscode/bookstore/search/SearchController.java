package com.amigoscode.bookstore.search;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

	private final BookSearch bookSearch;

	private final EmbeddingPlayground playground;

	public SearchController(BookSearch bookSearch, EmbeddingPlayground playground) {
		this.bookSearch = bookSearch;
		this.playground = playground;
	}

	@GetMapping("/api/search")
	public List<BookSearch.Result> search(@RequestParam String q, @RequestParam(required = false) Integer since,
			@RequestParam(defaultValue = "3") int k) {
		return since == null ? bookSearch.search(q, k) : bookSearch.searchPublishedSince(q, since, k);
	}

	@GetMapping("/api/similarity")
	public double similarity(@RequestParam String a, @RequestParam String b) {
		return playground.similarity(a, b);
	}

}
