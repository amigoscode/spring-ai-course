package com.amigoscode.bookstore;

public class InvalidOrderException extends RuntimeException {

	public InvalidOrderException(String message) {
		super(message);
	}

}
