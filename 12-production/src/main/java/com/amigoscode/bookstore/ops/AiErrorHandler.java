package com.amigoscode.bookstore.ops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

/**
 * When DeepSeek is down, slow or out of credit, users get a clear answer instead of a stack trace.
 */
@RestControllerAdvice
public class AiErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(AiErrorHandler.class);

	// TODO task-3: turn TransientAiException and ResourceAccessException (DeepSeek busy or unreachable)
	// and NonTransientAiException (bad key, no credit) into a 503 ProblemDetail with a friendly
	// message. Log the real error, but never send it to the user.

}
