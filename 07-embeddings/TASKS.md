# 07 · Embeddings

Start Docker before running the tests. The first test run downloads the embedding model.
Run every test in the module with `./mvnw test`, or one task with the command under it.

## Task 1: Your first embedding

Implement `EmbeddingPlayground.embed(String text)` with the injected `EmbeddingModel`.

**Done when:** `./mvnw test -Dtest=Task1EmbedTest` passes

<details>
<summary>Hint</summary>

`embeddingModel.embed(text)` returns a `float[]`.
</details>

## Task 2: Cosine similarity by hand

Implement `Similarity.cosine(float[] a, float[] b)`: the dot product divided by the product
of the two lengths. Do it in one loop.

**Done when:** `./mvnw test -Dtest=Task2CosineTest` passes (no Docker needed for this one)

<details>
<summary>Hint</summary>

In the loop, add up `a[i] * b[i]`, `a[i] * a[i]` and `b[i] * b[i]`. The lengths are the
square roots of the last two sums.
</details>

## Task 3: Semantic search without a database

Implement `EmbeddingPlayground.rank(query, candidates)`: embed the query, embed all
candidates **in one call**, and return the candidates sorted by similarity, highest first.

**Done when:** `./mvnw test -Dtest=Task3RankTest` passes

<details>
<summary>Hint</summary>

`embeddingModel.embed(List<String>)` embeds a batch. Pair each candidate with its score,
sort by score descending, then keep only the text.
</details>

## Task 4: Store the catalog in pgvector

1. In `application.properties`, make Spring AI create the `vector_store` table on startup.
2. Implement `CatalogIndexer.toDocument(Book)`: a `Document` with the id `idOf(book)`, the
   text `"<title>. <description>"`, and the title, author, year and topics as metadata.
3. In `CatalogIndexer.run`, add the documents to the vector store. Running it twice must
   not create duplicates.

**Done when:** `./mvnw test -Dtest=Task4IndexTest` passes

<details>
<summary>Hint</summary>

`spring.ai.vectorstore.pgvector.initialize-schema=true`. For the duplicates, delete the
same ids first: `vectorStore.delete(ids)`, then `vectorStore.add(documents)`.
</details>

## Task 5: Search by meaning

Implement `BookSearch.search(query, topK)` with `vectorStore.similaritySearch(...)`. Return
at most `topK` results and ignore anything below `MIN_SIMILARITY`.

**Done when:** `./mvnw test -Dtest=Task5And6SearchTest#task5*` passes

<details>
<summary>Hint</summary>

`SearchRequest.builder().query(query).topK(topK).similarityThreshold(MIN_SIMILARITY).build()`
</details>

## Task 6: Filter on metadata

Implement `searchPublishedSince(query, year, topK)`: like `search`, but only books published
in or after `year`.

**Done when:** `./mvnw test -Dtest=Task5And6SearchTest` passes

<details>
<summary>Hint</summary>

`.filterExpression("year >= " + year)`. Spring AI translates this portable expression into
a Postgres JSON query.
</details>

## Task 7: Look inside

1. Start the app and try the curl commands in the README.
2. In `psql`, run `select metadata->>'title', embedding <=> (select embedding from vector_store where metadata->>'title' = 'Clean Code') as distance from vector_store order by distance;`
   That's the similarity search, in plain SQL.

**Done when:** you've run a vector search in SQL.

## Break it

- Rank "I want to write my tests before my code" against the descriptions. This small model
  picks *Clean Code* over *TDD*. Small models are fast and free, but not perfect. How would
  you check whether a bigger model is worth it? (Module 11 answers that.)
- Search for "Kotlin". Nothing in the catalog is about Kotlin. What comes back, and with what
  scores? What threshold would you pick?
- Embed only the title, without the description. How much worse does search get?
