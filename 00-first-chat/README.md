# 00 · First Chat

Your first call to an LLM from Spring Boot. By the end of this module the Amigos Books
app answers questions over HTTP, and you can see what each answer cost in tokens.

## What you'll learn

- How Spring AI talks to a model: `ChatModel` and `ChatClient`
- How a Spring Boot starter configures DeepSeek from two properties
- What a token is, and why every call reports its token usage

## The idea

An LLM is a remote API. You send it text, it sends text back, and you pay per token
(a token is roughly 3/4 of a word). Spring AI hides the HTTP details behind two types:

```
Your code ──► ChatClient ──► ChatModel (DeepSeekChatModel) ──► api.deepseek.com
              fluent API      one per provider                 HTTP + JSON
```

- **`ChatModel`** is the low-level interface. Every provider (DeepSeek, OpenAI, Anthropic...)
  has one. The DeepSeek starter creates it for you from `application.properties`.
- **`ChatClient`** is the API you use every day. It's like `RestClient`, but for LLMs:

```java
String answer = chatClient.prompt()
        .user("Do you sell Effective Java?")
        .call()
        .content();
```

Swap the starter and the same code talks to a different provider. That's the point.

### Tokens

Every response carries a `Usage` object: **prompt tokens** (what you sent) and
**completion tokens** (what the model wrote). Providers charge for both. Get used to
looking at these numbers now, because in later modules (memory, RAG) the prompt grows,
and so does the bill.

## Run it

```bash
./mvnw spring-boot:run
curl "localhost:8080/api/chat?question=Recommend+one+book+about+Java"
```

This module needs no Docker. It only needs `DEEPSEEK_API_KEY` (see the repo README).

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [ChatClient API](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [DeepSeek Chat](https://docs.spring.io/spring-ai/reference/api/chat/deepseek-chat.html)
