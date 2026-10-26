# 04 · Chat Memory

Start Docker before running the tests. They start their own Postgres with Testcontainers.
Run every test in the module with `./mvnw test`, or one task with the command under it.

## Task 1: Remember the conversation

1. In the `BookAssistant` constructor, create a `MessageChatMemoryAdvisor` from the
   injected `ChatMemory`.
2. In `chat(conversationId, message)`, add the advisor to the request and pass the
   conversation id as the `ChatMemory.CONVERSATION_ID` advisor parameter.

**Done when:** `./mvnw test -Dtest=Task1And2MemoryTest#task1*` passes

<details>
<summary>Hint</summary>

```java
.advisors(a -> a.advisors(memoryAdvisor).param(ChatMemory.CONVERSATION_ID, conversationId))
```

Why not add it with `defaultAdvisors` on the builder? Try it, then call `ask(...)`. Every
request would need a conversation id, even the ones that aren't a chat.
</details>

## Task 2: Keep only the last 10 messages

In `ChatMemoryConfig`, define a `ChatMemory` bean: a `MessageWindowChatMemory` that keeps
`MAX_MESSAGES` messages and uses the `ChatMemoryRepository` Spring AI already created.

**Done when:** `./mvnw test -Dtest=Task1And2MemoryTest` passes

<details>
<summary>Hint</summary>

```java
MessageWindowChatMemory.builder().chatMemoryRepository(repository).maxMessages(MAX_MESSAGES).build()
```
</details>

## Task 3: Save it in Postgres

The JDBC memory starter is already in `pom.xml`, so the repository is a
`JdbcChatMemoryRepository`. But the table doesn't exist yet. In `application.properties`,
tell Spring AI to create its schema on startup.

**Done when:** `./mvnw test -Dtest=Task3PostgresMemoryTest` passes

<details>
<summary>Hint</summary>

`spring.ai.chat.memory.repository.jdbc.initialize-schema=always`. The default only creates
the schema for embedded databases like H2.
</details>

## Task 4: Show and forget a conversation

In `ConversationController`, add:

- `GET /api/conversations/{conversationId}`: the history as a JSON list of `HistoryEntry`
  (`role` is the message type, like `USER`)
- `DELETE /api/conversations/{conversationId}`: forgets it and returns `204 No Content`

**Done when:** `./mvnw test -Dtest=Task4ConversationEndpointTest` passes

<details>
<summary>Hint</summary>

`chatMemory.get(id)` and `chatMemory.clear(id)`. Each message has `getMessageType()` and `getText()`.
</details>

## Task 5: Try it for real

1. `./mvnw test -Plive`
2. Start the app, chat with the curl commands in the README, then **restart the app** and
   ask "What's my name?" again.
3. Open `psql` and read the `spring_ai_chat_memory` table.

**Done when:** the assistant remembers you after a restart.

## Break it

- Set `MAX_MESSAGES` to 2. Tell the assistant your name, ask two other questions, then ask
  for your name.
- Use the same conversation id for two different people. What could go wrong in a real app?
  Where should the conversation id come from? (Hint: who is logged in.)
- Have a 20 message conversation and watch the prompt tokens of each call grow.
