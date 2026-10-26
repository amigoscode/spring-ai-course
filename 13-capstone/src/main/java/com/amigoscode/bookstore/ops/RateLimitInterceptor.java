package com.amigoscode.bookstore.ops;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

	private final RateLimiter rateLimiter;

	public RateLimitInterceptor(RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (request.getUserPrincipal() == null || rateLimiter.tryAcquire(request.getUserPrincipal().getName())) {
			return true;
		}
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setHeader("Retry-After", String.valueOf(RateLimiter.WINDOW.toSeconds()));
		return false;
	}

}
