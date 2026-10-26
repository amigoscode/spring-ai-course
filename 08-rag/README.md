# 08 · RAG

The model knows a lot, but nothing about **your** shop: your return window, your shipping
prices, your loyalty programme. Fine-tuning a model on them is slow and expensive. Instead,
you look up the right passages and put them in the prompt. That's **Retrieval Augmented
Generation (RAG)**, and it's how most real AI features work.

**This module needs Docker running.**

## What you'll learn

- The ETL pipeline: read documents, split them into chunks, store them with metadata
- Naive RAG with `QuestionAnswerAdvisor`
- Showing **sources**, so users can check the answer
- Modular RAG with `RetrievalAugmentationAdvisor`: query rewriting, retrieval, augmentation
- What to do when nothing relevant is found
- Agentic RAG: letting the model decide when to search

## The idea

### Two phases

```
 ONCE (at startup, or when documents change)            EVERY QUESTION
 ──────────────────────────────────────────            ───────────────────────────────────────
 docs/*.md                                              "How long do I have to return a book?"
   │  Extract   MarkdownDocumentReader                     │  embed + similarity search
   ▼                                                       ▼
 sections (Documents + metadata)                        top 4 chunks about returns
   │  Transform TokenTextSplitter                          │  augment
   ▼                                                       ▼
 chunks of ~300 tokens                                  prompt = question + chunks + "only use this"
   │  Load      vectorStore.add(...)                       │
   ▼                                                       ▼
 Postgres (pgvector)  ◄───────────── searched by ───── DeepSeek answers from the chunks
```

### Why chunks?

A whole document is too big to embed well and too expensive to send every time. Small
chunks are precise, but lose context. ~300 tokens with clear headings is a good start.
Chunking is the setting that changes RAG quality the most. Experiment with it.

### Naive RAG in one line

```java
chatClient.prompt()
    .user(question)
    .advisors(QuestionAnswerAdvisor.builder(vectorStore).searchRequest(...).build())
    .call().content();
```

The advisor searches with the question, puts the results into the user message, and adds
"if the answer is not in the context, say you can't answer".

### When the question is messy

"hi!! my sister's birthday is friday, i'm so stressed, how fast can you get a book to her??"
is a terrible search query. `RetrievalAugmentationAdvisor` lets you add steps:

1. **Query transformation**: `RewriteQueryTransformer` asks the model to rewrite it as
   "express delivery time". That's an extra model call, and usually worth it.
2. **Retrieval**: `VectorStoreDocumentRetriever` with your filters.
3. **Augmentation**: `ContextualQueryAugmenter`. With `allowEmptyContext(false)`, if nothing
   relevant was found, the model is told to say it can't answer, instead of guessing.

### Agentic RAG

In tasks 2 to 4 *you* decide to search on every question. In task 5 search becomes a
**tool**, and the assistant decides when it needs it: policy questions trigger a search,
"hi" doesn't.

## Run it

```bash
./mvnw spring-boot:run
curl "localhost:8080/api/help?q=How+long+do+I+have+to+return+a+book"
curl "localhost:8080/api/help/advanced?q=my+sisters+birthday+is+friday+how+fast+can+you+deliver"
curl "localhost:8080/api/help/advanced?q=whats+a+good+chocolate+cake+recipe"
```

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Retrieval Augmented Generation](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)
- [ETL pipeline](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html)
