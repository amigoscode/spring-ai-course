# 04 · Chat Memory

In module 01 you sent the history by hand. Real chat apps need the model to remember
each user's conversation across requests and across restarts. Spring AI does this with an
**advisor** and a **chat memory** that you can store in Postgres.

**This module needs Docker running** (Docker Desktop on Windows and macOS).

## What you'll learn

- `ChatMemory` and `MessageChatMemoryAdvisor`
- Conversation ids: keeping each user's chat separate
- `MessageWindowChatMemory`: why you must limit how much history you send
- Saving memory in Postgres with the JDBC repository
- Docker Compose with Spring Boot, and Testcontainers in tests

## The idea

### An advisor wraps every call

An **advisor** is like a servlet filter for model calls. It can change the request before
it reaches the model and look at the response after. The memory advisor does this:

```
chat("conv-42", "What's my name?")
   │
   ▼  MessageChatMemoryAdvisor.before()
   │    1. load the messages of conv-42 from ChatMemory
   │    2. put them in front of the new question
   │    3. save the new question
   ▼
 DeepSeek sees:  SYSTEM ... │ USER "I'm Ana" │ ASSISTANT "Hi Ana!" │ USER "What's my name?"
   │
   ▼  MessageChatMemoryAdvisor.after()
        4. save the model's answer to conv-42
```

You tell it which conversation a request belongs to with an advisor parameter:

```java
.advisors(a -> a.advisors(memoryAdvisor).param(ChatMemory.CONVERSATION_ID, conversationId))
```

### A window, not everything

Every message you send back costs tokens, every time. After 100 turns you'd pay for 100
turns on every question, and you'd eventually hit the model's context limit.
`MessageWindowChatMemory` keeps only the last N messages. The system prompt is always kept.

### Where memory lives

`ChatMemory` decides **what** to keep (the window). A `ChatMemoryRepository` decides
**where**: in memory by default, or in a database. Add the JDBC starter and Spring AI
uses Postgres instead. Restart the app and the conversations are still there.

```
ChatMemory (MessageWindowChatMemory, last 10)
   └── ChatMemoryRepository (JdbcChatMemoryRepository)
          └── Postgres table SPRING_AI_CHAT_MEMORY
```

### Docker, without the hassle

- **Running the app:** `spring-boot-docker-compose` starts `compose.yaml` when the app
  starts and stops it when the app stops. No `docker compose up` needed. Postgres gets a
  random free port, and Spring Boot connects to it for you.
- **Tests:** Testcontainers starts a fresh Postgres per test run (`TestcontainersConfig`).

### A warning before you copy this into a real app

Here anyone who knows (or guesses) a conversation id can read it, delete it or write into it.
That keeps the module focused on memory, but it's an
[IDOR](https://owasp.org/Top10/A01_2021-Broken_Access_Control/) in a real app. Tie every
conversation to the logged-in user: build the memory key from the user's id, and check
ownership on every endpoint. Module 06 starts doing this, and module 12 adds Spring Security.

## Run it

```bash
./mvnw spring-boot:run
curl -X POST -H "Content-Type: text/plain" localhost:8080/api/conversations/ana -d "Hi, I'm Ana and I like concurrency"
curl -X POST -H "Content-Type: text/plain" localhost:8080/api/conversations/ana -d "What's my name, and which book fits me?"
curl localhost:8080/api/conversations/ana
```

Look inside the database while the app is running:

```bash
docker compose exec postgres psql -U bookstore -c "select conversation_id, type, content from spring_ai_chat_memory"
```

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Chat memory](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)
- [Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)
- [Spring Boot Docker Compose support](https://docs.spring.io/spring-boot/reference/features/dev-services.html)
