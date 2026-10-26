# 12 · Production

Start Docker before running the tests.
Run every test in the module with `./mvnw test`, or one task with the command under it.

## Task 1: Logins

In `SecurityConfig`, define:

1. A `SecurityFilterChain`: `/actuator/health` and the read-only MCP server (`/mcp`) are public,
   the other `/actuator` endpoints and `/api/usage` are for `ADMIN` only, everything else needs a login with
   HTTP Basic. It's a stateless JSON API: no sessions, and CSRF off.
2. A `UserDetailsService` with `ana` / `ana-password` and `bob` / `bob-password` (role
   `CUSTOMER`), and `admin` / `admin-password` (role `ADMIN`). Encode the passwords with the
   `PasswordEncoder` bean.

**Done when:** `./mvnw test -Dtest=Task1SecurityTest` passes

<details>
<summary>Hint</summary>

```java
http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health").permitAll()
        ...
        .anyRequest().authenticated())
    .httpBasic(Customizer.withDefaults())
    .csrf(csrf -> csrf.disable())
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .build();
```
</details>

## Task 2: Your data is yours

1. In `AssistantController`, act for the logged-in customer (`Principal`), not a header.
2. In `ConversationController.memoryKey`, build the key from the user's name and the conversation
   id, as a name based UUID. All three endpoints already use it.

**Done when:** `./mvnw test -Dtest=Task2OwnershipTest` passes

<details>
<summary>Hint</summary>

`UUID.nameUUIDFromBytes(("chat:" + principal.getName() + ":" + conversationId).getBytes(UTF_8))`.
`BookAssistant.assist` already does the same for its own conversations.
</details>

## Task 3: Fail fast, fail politely

1. In `application.properties`, retry at most 3 times, starting at 500ms, doubling, at most 5s.
2. In `AiErrorHandler`, turn `TransientAiException` and `ResourceAccessException` into a 503
   `ProblemDetail` that says to try again, and `NonTransientAiException` into a 503 that doesn't.
   Log the real error. Never put it in the response.

**Done when:** `./mvnw test -Dtest=Task3ResilienceTest` passes

<details>
<summary>Hint</summary>

The properties are under `spring.ai.retry` (`max-attempts`, `backoff.initial-interval`,
`backoff.multiplier`, `backoff.max-interval`). For the handler:
`@ExceptionHandler({ TransientAiException.class, ResourceAccessException.class })`.
</details>

## Task 4: Rate limiting

Implement `RateLimiter.tryAcquire(user)` as a sliding window: keep each user's request times,
drop the ones older than `WINDOW`, refuse if there are already `MAX_REQUESTS`, otherwise record
this one. `RateLimitInterceptor` already applies it to every `/api` request.

**Done when:** `./mvnw test -Dtest=Task4RateLimitTest` passes

<details>
<summary>Hint</summary>

An `ArrayDeque<Instant>` per user. Use `clock.instant()`, never `Instant.now()`, so the test can
move time forward. Two requests from the same user can arrive at once: `synchronized` on the deque.
</details>

## Task 5: Don't pay twice

Cache `PolicyAssistant.answerWithSources` in the `policy-answers` cache, keyed by the question
in lower case without surrounding spaces. Caching is already enabled on the application.

**Done when:** `./mvnw test -Dtest=Task5CacheTest` passes

<details>
<summary>Hint</summary>

`@Cacheable(cacheNames = "policy-answers", key = "#question.strip().toLowerCase()")`
</details>

## Task 6: Look at the numbers

1. Start the app and run the curl commands in the README.
2. As `admin`, open `/actuator/metrics/gen_ai.client.token.usage` and
   `/actuator/metrics/gen_ai.client.operation`. Use `?tag=gen_ai.token.type:input` to filter.
3. Ask the same policy question twice through `/api/help` and check the token count didn't move
   the second time.

**Done when:** you can say how many tokens your app has used, and how long a model call takes.

## Break it

- Comment out `.anyRequest().authenticated()`. Which tests fail? What could an anonymous user do?
- Cache `BookAssistant.assist` the same way. Why is that a data leak?
- Set `spring.ai.retry.max-attempts=10` and point `spring.ai.deepseek.base-url` at
  `http://localhost:9`. How long until the user gets an answer?
