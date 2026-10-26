package com.amigoscode.bookstore.gifts;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * The contract. Don't change it: the acceptance tests use it.
 *
 * @param recipient who the gift is for, in the customer's words ("my nephew, he just started a Java bootcamp")
 * @param occasion  birthday, graduation, ...
 * @param budget    the most the customer wants to spend on all the books together
 */
public record GiftRequest(
		@NotBlank @Size(max = 500) String recipient,
		@Size(max = 100) String occasion,
		@NotNull @DecimalMin("10.00") @DecimalMax("500.00") BigDecimal budget) {
}
