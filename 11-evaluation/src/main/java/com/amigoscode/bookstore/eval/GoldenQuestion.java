package com.amigoscode.bookstore.eval;

import java.util.List;

/**
 * One question with a known good answer: which document holds it, and the facts the
 * answer must mention. A set of these is your "golden dataset".
 */
public record GoldenQuestion(String question, String expectedSource, List<String> mustMention) {
}
