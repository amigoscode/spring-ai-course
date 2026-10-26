# 03 · Structured Output

So far the model has returned text. Your code can't do much with a paragraph. It needs
objects. In this module the model returns Java records, lists, and validated orders
extracted from messy customer emails.

## What you'll learn

- `.entity(MyRecord.class)`: from model text to a Java object
- `ParameterizedTypeReference` for lists and generic types
- How Spring AI tells the model which JSON to write (it's just the prompt)
- `validateSchema()`: retrying automatically when the JSON is wrong
- Why valid JSON is not the same as valid data, and how to check it

## The idea

### There's no magic, just a longer prompt

When you call `.entity(BookRecommendation.class)`, Spring AI:

1. Builds a **JSON schema** from the record
2. **Appends it to your user message**: "Your response should be in JSON format... here is the schema"
3. Calls the model as usual
4. Strips any `` ```json `` fences and parses the answer with Jackson

```
user message:  "Recommend one book about java."
             + "Your response should be in JSON format. ... {"title": ..., "author": ..., "reason": ...}"
model:         {"title": "Effective Java", "author": "Joshua Bloch", "reason": "..."}
your code:     BookRecommendation[title=Effective Java, author=Joshua Bloch, reason=...]
```

So the model can still get it wrong: chatty text before the JSON, a missing field,
a number written as a word.

### Retrying with the error

`.entity(OrderRequest.class, spec -> spec.validateSchema())` checks the answer against the
schema. If it fails, Spring AI sends the error back to the model and asks again, up to
3 times. You pay for each attempt.

### Valid JSON, wrong data

The schema says `quantity` is a number. It doesn't say "between 1 and 10" or "a book we
actually sell". Treat model output like any **untrusted user input**: validate it with
Bean Validation and your own business rules before you act on it.

## Run it

```bash
./mvnw spring-boot:run
curl "localhost:8080/api/recommendations/list?topic=java&max=2"
curl -X POST -H "Content-Type: text/plain" localhost:8080/api/orders/extract \
  -d "hi, 2 copies of effective java to Lisbon please. ana@example.com"
```

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Structured output](https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html)
- [ChatClient: entity](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_returning_an_entity)
