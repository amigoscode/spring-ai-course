# 02 · Prompt Templates

Prompts are code. Long prompts hidden in Java strings are hard to read, review and change.
In this module you move them into template files, fill them with data, and teach the model
an exact output format with examples.

## What you'll learn

- Keeping prompts in `src/main/resources/prompts/*.st` files
- Template variables: `{topic}`, `{maxBooks}`, `{catalog}`
- **Prompt stuffing**: putting your own data (the book catalog) into the prompt
- **Few-shot prompting**: showing examples instead of describing the format
- Overriding the system prompt for one request

## The idea

### Templates

Spring AI renders prompts with [StringTemplate](https://www.stringtemplate.org/).
A `{name}` in the file is replaced by a value you pass:

```
prompts/recommend.st                       Java
──────────────────────────────            ─────────────────────────────────
Recommend up to {maxBooks} books   ◄────   .param("maxBooks", 2)
about {topic} from the catalog.    ◄────   .param("topic", "testing")
```

```java
chatClient.prompt()
        .user(u -> u.text(recommendPrompt).param("topic", topic).param("maxBooks", 2))
        .call()
        .content();
```

`recommendPrompt` is a Spring `Resource`, injected with
`@Value("classpath:prompts/recommend.st")`.

### Prompt stuffing

The model knows nothing about *your* shop. The simplest fix is to paste your data into
the system prompt: here, the whole catalog. It works well for 10 books, but every request
pays for those tokens, and it can't scale to 10,000 books. That's the problem RAG solves
in module 08.

### Few-shot prompting

Describing a format ("reply with one word") works most of the time. Showing it works
better. You send fake earlier turns where the "assistant" already answered perfectly:

```
SYSTEM     Reply with one word: POSITIVE, NEGATIVE or NEUTRAL.
USER       Review: Every chapter is gold.
ASSISTANT  POSITIVE
USER       Review: The binding fell apart.
ASSISTANT  NEGATIVE
USER       Review: <the real review>         ← the model copies the pattern
```

For a classification like this, use **temperature 0** so the same review always gets
the same label.

## Run it

```bash
./mvnw spring-boot:run
curl "localhost:8080/api/recommendations?topic=concurrency&max=2"
curl -X POST localhost:8080/api/reviews/sentiment -d "Best Java book I own"
```

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Prompts and templates](https://docs.spring.io/spring-ai/reference/api/prompt.html)
- [ChatClient: prompt templates](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_prompt_templates)
