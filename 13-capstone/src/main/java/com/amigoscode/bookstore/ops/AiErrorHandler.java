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

	@ExceptionHandler({ TransientAiException.class, ResourceAccessException.class })
	ProblemDetail modelUnavailable(RuntimeException e) {
		log.warn("The model is unavailable after retries: {}", e.getMessage());
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
				"The assistant is busy right now. Please try again in a minute.");
		problem.setTitle("Assistant unavailable");
		return problem;
	}

	@ExceptionHandler(NonTransientAiException.class)
	ProblemDetail modelRejected(NonTransientAiException e) {
		// Bad API key, no credit, invalid request: retrying won't help. Alert someone.
		log.error("The model rejected the request: {}", e.getMessage());
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
				"The assistant is not available. We've been told and are looking into it.");
		problem.setTitle("Assistant unavailable");
		return problem;
	}

}
