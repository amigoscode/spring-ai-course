# 07 · Embeddings

The catalog has 10 books, so you could paste it into the prompt. A real shop has 100,000.
To find the right ones you need search by **meaning**: "a book about writing tests first"
must find *Test Driven Development* even though the words don't match. That's what
embeddings do.

**This module needs Docker running.** The first run downloads the embedding model
(about 90 MB) and caches it in `~/.cache/spring-ai-onnx`.

## What you'll learn

- What an embedding is, and why similar meanings give similar vectors
- Cosine similarity, written by hand
- Running an embedding model **locally** with ONNX: free, offline, no API key
- Postgres as a vector database with pgvector
- `VectorStore`: storing documents and searching them by similarity
- Metadata filters

## The idea

### Text in, numbers out

An embedding model turns text into a fixed-length list of numbers (here 384). Texts with
similar meanings end up pointing in similar directions:

```
"write the test before the code"        → [ 0.12, -0.03, 0.44, ... ]  ┐ close
"TDD: red, green, refactor"             → [ 0.10, -0.01, 0.41, ... ]  ┘
"chocolate cake with strawberries"      → [-0.31,  0.27, 0.02, ... ]    far away
```

**Cosine similarity** measures how close two directions are: 1 is the same meaning,
0 is unrelated.

```
cos(a, b) = (a · b) / (|a| × |b|)
```

### Two different models

DeepSeek doesn't offer embeddings, and you don't need it to. Embedding is a different, much
smaller job, done by a different model:

| Job | Model | Where it runs |
|---|---|---|
| Chat, reasoning, tools | DeepSeek | DeepSeek's servers, paid per token |
| Turning text into vectors | `all-MiniLM-L6-v2` (ONNX) | Inside your JVM, free |

Spring AI hides both behind interfaces: `ChatModel` and `EmbeddingModel`.

### A vector database is Postgres plus an index

You can't compare a query against a million vectors one by one (task 3 does that for 10).
pgvector adds a `vector` column type and an index (HNSW) that finds the nearest vectors fast.
Spring AI's `PgVectorStore` uses your `EmbeddingModel` for you:

```
vectorStore.add(documents)
   → EmbeddingModel.embed(each document's text)
   → INSERT INTO vector_store (id, content, metadata, embedding)

vectorStore.similaritySearch("write tests first")
   → EmbeddingModel.embed("write tests first")
   → SELECT ... ORDER BY embedding <=> :query_vector LIMIT k
```

Two rules that bite everyone once:

- Documents and queries must be embedded with the **same model**.
- The vector size (384) is fixed when the table is created. Change the model and you
  must recreate the table and re-embed everything.

### Text vs metadata

The **text** is what gets embedded and searched. **Metadata** (author, year, topics) is
stored next to it for filtering (`year >= 2015`) and for showing results.

## Run it

```bash
./mvnw spring-boot:run
curl "localhost:8080/api/search?q=running+code+on+many+threads"
curl "localhost:8080/api/search?q=how+to+design+good+software&since=2015"
curl "localhost:8080/api/similarity?a=cheap+flights&b=low+cost+plane+tickets"
```

Then look at the vectors themselves:

```bash
docker compose exec postgres psql -U bookstore -c "select metadata->>'title', vector_dims(embedding) from vector_store"
```

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Embeddings API](https://docs.spring.io/spring-ai/reference/api/embeddings.html)
- [Transformers (ONNX) embeddings](https://docs.spring.io/spring-ai/reference/api/embeddings/onnx.html)
- [PGvector vector store](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)
