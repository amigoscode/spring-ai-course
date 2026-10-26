# 05 · Advisors

Start Docker before running the tests.
Run every test in the module with `./mvnw test`, or one task with the command under it.

## Task 1: Track what you spend

1. Implement `TokenUsageAdvisor.adviseCall`: call the rest of the chain, then record the
   response's prompt and completion tokens in the `TokenUsageTracker`.
2. In the `BookAssistant` constructor, add a `TokenUsageAdvisor` to the builder's
   **default** advisors, so every request is counted.

**Done when:** `./mvnw test -Dtest=Task1TokenUsageTest` passes

<details>
<summary>Hint</summary>

`chain.nextCall(request)` returns the `ChatClientResponse`. The usage is in
`response.chatResponse().getMetadata().getUsage()`. Register it with
`.defaultAdvisors(new TokenUsageAdvisor(tokenUsageTracker))`.
</details>

## Task 2: Never send card details to the model

Add Spring AI's `SafeGuardAdvisor` to the default advisors. It must block `SENSITIVE_WORDS`,
answer with `SENSITIVE_REFUSAL`, and run with order `Ordered.HIGHEST_PRECEDENCE + 1`.

**Done when:** `./mvnw test -Dtest=Task2SafeGuardTest` passes

<details>
<summary>Hint</summary>

`SafeGuardAdvisor.builder().sensitiveWords(...).failureResponse(...).order(...).build()`.
`defaultAdvisors(...)` takes several advisors at once.
</details>

## Task 3: Write a guard

1. Implement `PromptInjectionGuardAdvisor.adviseCall`. If the user message matches any of
   `PATTERNS`, **don't call the chain**. Return a `ChatClientResponse` whose answer is `REFUSAL`.
2. Add it to the default advisors.

Look at its `getOrder()`. Why must it run before the memory advisor?

**Done when:** `./mvnw test -Dtest=Task3PromptInjectionTest` passes

<details>
<summary>Hint</summary>

```java
ChatResponse refusal = ChatResponse.builder()
    .generations(List.of(new Generation(new AssistantMessage(REFUSAL))))
    .build();
return ChatClientResponse.builder().chatResponse(refusal).context(Map.copyOf(request.context())).build();
```
</details>

## Task 4: Rewrite the request

1. Implement `ReReadingAdvisor.before`. Change the user message to
   `<question>\nRead the question again: <question>`.
2. In `answerCarefully`, add a `ReReadingAdvisor` to **that request only**.

**Done when:** `./mvnw test -Dtest=Task4ReReadingTest` passes

<details>
<summary>Hint</summary>

`request.mutate().prompt(request.prompt().augmentUserMessage(newText)).build()`
</details>

## Task 5: Try it for real

1. `./mvnw test -Plive`
2. Start the app, ask a few questions, then `curl localhost:8080/api/usage`.
3. Look at DeepSeek's pricing page and work out what your app has cost so far.

**Done when:** you know your app's cost per question.

## Break it

- Get past your own guard. Rephrase "ignore your instructions" ten different ways. How
  many get through? Did the system prompt hold anyway?
- Swap the orders of the guard and the memory advisor. Send an attack in `chat(...)` and look
  at the `spring_ai_chat_memory` table.
- Ask `answerCarefully` a long question. How many extra prompt tokens did re-reading cost?
