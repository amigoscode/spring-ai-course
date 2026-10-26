# 12 · Production

The assistant works. Now make it safe to put in front of real customers: they log in, they
can only see their own data, a DeepSeek outage doesn't take the app down, one user can't run
up the bill, and you can see what it all costs.

**This module needs Docker running.**

## What you'll learn

- Spring Security: the customer is the logged-in user, never a header
- Closing the conversation IDOR from modules 04 to 11
- Retries that fail fast, and friendly errors when the model is down
- Rate limiting per user
- Caching answers to save money
- Metrics: token usage and latency from Spring AI's observations
- Keeping personal data out of logs and traces

## The idea

### Identity comes from the server

Until now, `X-Customer-Id` said who the customer was. Anyone could send `X-Customer-Id: ana`.
Now Spring Security authenticates the request, and controllers take the `Principal`:

```java
public String assist(Principal principal, ...) {
    return bookAssistant.assist(principal.getName(), ...);   // the model can't change this
}
```

Every conversation key is built from the principal too, so `/api/conversations/{id}` only ever
reaches **your** conversations, whatever id you send.

### Failing well

| Problem | Spring AI throws | What the user sees |
|---|---|---|
| DeepSeek overloaded, timeout, 5xx | `TransientAiException` (after retries) | 503 "busy, try again" |
| Network down | `ResourceAccessException` | 503 "busy, try again" |
| Bad API key, no credit, invalid request | `NonTransientAiException` (no retries) | 503 "not available", and you get an error log |

Spring AI's default retry is **10 attempts with up to 3 minutes between them**: one request could
hang for over 10 minutes while holding a thread. Three quick attempts and an honest error is better.
Never send the real error to the user: it can contain request ids, URLs or worse.

### Limits

- **Rate limit:** 10 model-backed requests per user per minute, then `429 Too Many Requests`
  with `Retry-After`. It protects your bill and DeepSeek's rate limit from one noisy (or
  compromised) account.
- **Cache:** the same policy question asked twice in an hour costs one model call, not two.
  Cache only what's the same for everyone. Never cache answers built from someone's private data.

### Seeing what happens

Spring AI publishes Micrometer metrics for every call, and Actuator serves them:

```
gen_ai.client.token.usage    tokens, by model and by input/output: your bill
gen_ai.client.operation      how long each model call took
spring.ai.chat.client        how long each ChatClient call took, advisors included
spring.ai.advisor            how long each advisor took
```

Send them to Prometheus and Grafana, or any Micrometer backend, and you have a cost dashboard.

Prompts and answers are **not** logged in traces by default (`log-prompt=false`). Keep it that
way in production: they're full of personal data.

## Run it

```bash
./mvnw spring-boot:run
curl -i "localhost:8080/api/chat?question=hi"                          # 401
curl -u ana:ana-password "localhost:8080/api/chat?question=Any+Java+books"
curl -u admin:admin-password localhost:8080/actuator/metrics/gen_ai.client.token.usage
for i in $(seq 1 11); do curl -s -o /dev/null -w "%{http_code} " -u bob:bob-password localhost:8080/api/conversations/mine; done
```

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Spring Security servlet configuration](https://docs.spring.io/spring-security/reference/servlet/configuration/java.html)
- [Spring AI observability](https://docs.spring.io/spring-ai/reference/observability/index.html)
- [OWASP Top 10 for LLM applications](https://genai.owasp.org/llm-top-10/)
