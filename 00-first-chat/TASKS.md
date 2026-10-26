# 00 · First Chat

Work through the tasks in order. Each one has a test that fails until the task is done.
Run every test in the module with `./mvnw test`, or one task with the command under it.

## Task 1: Connect to DeepSeek

In `src/main/resources/application.properties`, configure the DeepSeek starter:

- the API key, read from the `DEEPSEEK_API_KEY` environment variable
- the chat model `deepseek-v4-flash`

Never paste the key itself into the file. Use a `${...}` placeholder.

**Done when:** `./mvnw test -Dtest=Task1DeepSeekConfigTest` passes

<details>
<summary>Hint</summary>

The properties live under `spring.ai.deepseek`. The key is `spring.ai.deepseek.api-key`
and the model is `spring.ai.deepseek.chat.model`.
</details>

## Task 2: Build a ChatClient

Spring AI auto-configures a `ChatClient.Builder`. In the `BookAssistant` constructor,
use it to create the `ChatClient`.

**Done when:** `./mvnw test -Dtest=Task2And3AskTest#task2*` passes

<details>
<summary>Hint</summary>

It's one line: `builder.build()`.
</details>

## Task 3: Ask a question

Implement `BookAssistant.ask(String question)`. Send the question as a **user** message
and return the model's text.

**Done when:** `./mvnw test -Dtest=Task2And3AskTest` passes

<details>
<summary>Hint</summary>

`chatClient.prompt().user(...).call().content()`
</details>

## Task 4: Count the tokens

Implement `BookAssistant.askWithUsage(String question)`. Instead of `.content()`, ask for
the whole `ChatResponse`. Read the answer text and the `Usage` from it, log the token
counts, and return them in an `Answer`.

**Done when:** `./mvnw test -Dtest=Task4UsageTest` passes

<details>
<summary>Hint</summary>

`.call().chatResponse()` gives you the response. The text is in
`response.getResult().getOutput().getText()` and the usage is in
`response.getMetadata().getUsage()`.
</details>

## Task 5: Expose it over HTTP

In `ChatController`, add `GET /api/chat?question=...` that calls `askWithUsage` and returns
the `Answer` as JSON.

**Done when:** `./mvnw test -Dtest=Task5ChatEndpointTest` passes

<details>
<summary>Hint</summary>

`@GetMapping("/api/chat")` and a `@RequestParam String question` parameter.
</details>

## Task 6: Talk to the real model

The tests above use a fake model, so they're free and work offline. Now try the real one:

1. Run the live test: `./mvnw test -Plive` (it costs a fraction of a cent)
2. Start the app with `./mvnw spring-boot:run` and call it with curl
3. Ask a short question, then a long one. Compare the token counts.

**Done when:** you've seen real token counts in your terminal.

## Break it

- Remove your API key and start the app. Read the error. Where does it come from?
- Ask the same question three times. Are the answers identical? Why not?
