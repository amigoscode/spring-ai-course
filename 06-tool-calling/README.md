# 06 · Tool Calling

Until now the assistant could only talk. With tools it can **act**: check live stock,
search the catalog, place an order, look up an order. This is the step from chatbot to
agent, and the step where security starts to really matter.

**This module needs Docker running.**

## What you'll learn

- `@Tool` and `@ToolParam`: turning Java methods into tools
- The tool loop: what really happens between your code, Spring AI and the model
- `ToolContext`: passing data to tools that the model must never control
- Authorization inside tools
- What happens when a tool throws

## The idea

### The model never runs your code

The model can't call Java. It can only **ask** for a tool to be called, by name, with
JSON arguments. Spring AI runs the method and sends the result back. That loop repeats
until the model answers with text.

```
you ──► ChatClient ──► DeepSeek    "Is Clean Code in stock?" + the list of tools
                       DeepSeek ──► "call checkStock {"title": "Clean Code"}"
        Spring AI runs BookstoreTools.checkStock("Clean Code") → {"copies": 3}
        ChatClient ──► DeepSeek    ... + the tool result
                       DeepSeek ──► "Yes, we have 3 copies!"
you ◄── "Yes, we have 3 copies!"
```

That's **two model calls** for one question, and the second one carries the whole
conversation again. Watch the `TokenUsageAdvisor` log: each round is counted.

### Descriptions are the API

The model picks tools by reading their descriptions and parameter schemas. A vague
description means wrong calls. Write them like documentation for a new colleague:
what it does, when to use it, what the arguments mean.

```java
@Tool(description = "Check how many copies of a book are in stock right now. Use the exact catalog title.")
public StockInfo checkStock(@ToolParam(description = "The exact title of the book") String title) { ... }
```

### Never let the model decide who the user is

Everything the model sends is **untrusted input**. A user can talk the model into anything
("place the order for customer-99"). So:

- **Who** is acting comes from your app (the logged-in user), passed in `ToolContext`.
  It's never a tool parameter, so the model can't set it.
- Tools **check authorization** themselves: `orderStatus` only shows the caller's own orders.
- Tools **validate** like any API: quantity limits, stock, existing titles.
- **Memory belongs to one customer too.** `assist` builds the memory key from the customer
  and the conversation id, so guessing someone's conversation id gets you nothing. (It's
  hashed into a UUID because Spring AI's `conversation_id` column holds 36 characters.)
- Risky actions ask for **confirmation** first (in the system prompt, and in real apps
  often a human approval step).

```java
.tools(tools)
.toolContext(Map.of("customerId", customerId))   // from Spring Security in a real app
```

### When a tool throws

The exception message goes back to the model as the tool result ("Not enough stock for
Clean Code: 3 left, 9 asked"), and the model explains it to the user. Your app doesn't crash.

## Run it

```bash
./mvnw spring-boot:run
curl -X POST -H "Content-Type: text/plain" -H "X-Customer-Id: ana" localhost:8080/api/assistant/c1 -d "Is Refactoring in stock?"
curl -X POST -H "Content-Type: text/plain" -H "X-Customer-Id: ana" localhost:8080/api/assistant/c1 -d "Great, I want 2 copies"
curl -X POST -H "Content-Type: text/plain" -H "X-Customer-Id: ana" localhost:8080/api/assistant/c1 -d "Yes, confirmed"
```

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Tool calling](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [OWASP Top 10 for LLM apps: excessive agency](https://genai.owasp.org/llmrisk/llm062025-excessive-agency/)
