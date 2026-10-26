package com.amigoscode.bookstore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * An order extracted from a customer's free text email.
 */
public record OrderRequest(
		@NotBlank String bookTitle,
		@Min(1) @Max(10) int quantity,
		@NotBlank String deliveryCity,
		@Email String customerEmail) {
}
