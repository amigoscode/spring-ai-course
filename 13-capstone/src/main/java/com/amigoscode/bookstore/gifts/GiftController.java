package com.amigoscode.bookstore.gifts;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GiftController {

	private final GiftFinder giftFinder;

	public GiftController(GiftFinder giftFinder) {
		this.giftFinder = giftFinder;
	}

	// TODO capstone: POST /api/gifts, for logged-in customers, with a validated GiftRequest

}
