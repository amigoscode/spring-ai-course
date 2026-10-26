# 03 · Structured Output

Run every test in the module with `./mvnw test`, or one task with the command under it.

## Task 1: Return a record

Implement `BookAssistant.recommendOne(String topic)`. Ask for one book about the topic from
the catalog and return it as a `BookRecommendation`.

**Done when:** `./mvnw test -Dtest=Task1And2RecommendationTest#task1*` passes

<details>
<summary>Hint</summary>

Replace `.content()` with `.entity(BookRecommendation.class)`.
</details>

## Task 2: Return a list

Implement `recommendMany(String topic, int maxBooks)` so it returns a
`List<BookRecommendation>`. The prompt must mention the topic and the number.

**Done when:** `./mvnw test -Dtest=Task1And2RecommendationTest` passes

<details>
<summary>Hint</summary>

`List<BookRecommendation>.class` isn't valid Java, because of type erasure. Use
`.entity(new ParameterizedTypeReference<List<BookRecommendation>>() {})`.
</details>

## Task 3: Extract an order, with retries

Implement `OrderExtractor.extract(String email)`. Send the email as the user message and
return an `OrderRequest`. When the model's answer doesn't match the schema, Spring AI must
retry with the error.

**Done when:** `./mvnw test -Dtest=Task3ExtractOrderTest` passes

<details>
<summary>Hint</summary>

`.entity(OrderRequest.class, spec -> spec.validateSchema())`
</details>

## Task 4: Don't trust the model

Implement the TODO in `extractValidOrder`:

1. Validate the order with the injected `Validator`. `OrderRequest` already has the
   annotations (`@NotBlank`, `@Min(1)`, `@Max(10)`, `@Email`).
2. Check the book exists with `catalog.findByTitle(...)`.
3. If anything is wrong, throw `InvalidOrderException` with a message that names the
   problem field (or the unknown title).

**Done when:** `./mvnw test -Dtest=Task4ValidateOrderTest` passes

<details>
<summary>Hint</summary>

`validator.validate(order)` returns a `Set<ConstraintViolation<OrderRequest>>`. Each
violation has `getPropertyPath()` and `getMessage()`.
</details>

## Task 5: Try it for real

1. `./mvnw test -Plive`
2. Start the app and send `/api/orders/extract` the messiest email you can write:
   typos, slang, two books, no city.
3. Look at the user message Spring AI sends. Add `logging.level.org.springframework.ai=debug`
   to `application.properties`, or set a breakpoint in `FakeChatModel.call`.

**Done when:** you've seen the JSON schema inside the prompt.

## Break it

- Ask for an order of "a dozen copies". What does the model put in `quantity`?
- Send an email that orders two different books. `OrderRequest` holds one. What happens?
  How would you change the record?
- Remove `validateSchema()` and make the model chatty with a system prompt like "Always
  start your answer with a friendly greeting". Does parsing still work?
