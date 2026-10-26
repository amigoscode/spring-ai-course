package com.amigoscode.bookstore;

/**
 * One recommended book. Spring AI turns this record into a JSON schema for the model,
 * and the model's JSON answer back into this record.
 */
public record BookRecommendation(String title, String author, String reason) {
}
