# 01 · ChatClient in Depth

Module 00 sent a bare question. Real apps shape every request: who the assistant is,
how creative it may be, how long it may talk, and whether the answer arrives all at once
or word by word.

## What you'll learn

- **Message roles**: `system`, `user` and `assistant`, and what each one is for
- **Defaults vs per-request settings** on the `ChatClient`
- **Options**: `temperature` and `maxTokens`
- **Streaming** answers to the browser with Server-Sent Events
- **Why the model has no memory**, and how to send history yourself

## The idea

### A prompt is a list of messages

```
SYSTEM     "You are the assistant of Amigos Books..."   ← rules, set by you
USER       "My name is Ana."                            ← earlier turn
ASSISTANT  "Nice to meet you, Ana!"                     ← earlier turn
USER       "What's my name?"                            ← the new question
```

The **system** message sets behaviour for the whole conversation. Users never see it.
Set it once with `defaultSystem(...)` on the builder, and every request gets it.

### Options

- **temperature**: how random the next word is. Low (0 to 0.3) for factual answers,
  high (1 and above) for creative writing.
- **maxTokens**: a hard cap on the answer length. It protects your bill.

`defaultOptions(...)` applies to every request. `.options(...)` on one request overrides
the defaults for that request only.

### Streaming

`.call()` waits for the full answer. `.stream()` returns a `Flux<String>` that emits
pieces as the model writes them. Return that `Flux` from a controller with
`produces = TEXT_EVENT_STREAM` and the browser shows the text as it's typed, like ChatGPT.

### No memory

The model is **stateless**. Each call starts from zero. Chat apps "remember" because they
send the earlier messages again with every new question. You'll do that by hand in task 5,
and in module 04 Spring AI will do it for you.

## Run it

```bash
./mvnw spring-boot:run
curl "localhost:8080/api/chat?question=What+is+a+good+first+Java+book"
curl "localhost:8080/api/blurb?title=Clean+Code"
curl -N "localhost:8080/api/chat/stream?question=Tell+me+about+Effective+Java"
```

`-N` turns off curl's buffering so you can watch the stream arrive.

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [ChatClient API](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Prompts and message roles](https://docs.spring.io/spring-ai/reference/api/prompt.html)
