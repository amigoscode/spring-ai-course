# 08 · RAG

Start Docker before running the tests.
Run every test in the module with `./mvnw test`, or one task with the command under it.

The shop's policies are in `src/main/resources/docs/`. Read one before you start.

## Task 1: The ETL pipeline

In `PolicyIngestor`:

1. `read(resource)`: read the Markdown file with a `MarkdownDocumentReader`. Add the metadata
   `type` = `policy` and `source` = the file name to every document.
2. `run(...)`: delete the old policy documents from the vector store, then add the split chunks.

**Done when:** `./mvnw test -Dtest=Task1IngestTest` passes

<details>
<summary>Hint</summary>

```java
var config = MarkdownDocumentReaderConfig.builder()
    .withAdditionalMetadata("type", "policy")
    .withAdditionalMetadata("source", resource.getFilename())
    .build();
return new MarkdownDocumentReader(resource, config).get();
```

`vectorStore.delete("type == 'policy'")` deletes by metadata.
</details>

## Task 2: Naive RAG

Implement `PolicyAssistant.answer(question)` with a `QuestionAnswerAdvisor`. It must search
**only the policies** (not the books), using `policySearch(...)`.

**Done when:** `./mvnw test -Dtest=Task2And3QuestionAnswerTest#task2*` passes

<details>
<summary>Hint</summary>

The `questionAnswerAdvisor()` helper is already there. Add it with `.advisors(...)`.
</details>

## Task 3: Show your sources

Implement `answerWithSources(question)`. Ask for the `ChatClientResponse` instead of the
content. The advisor stores the documents it found in the response context under
`QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS`. Return the answer and the distinct `source` of
those documents.

**Done when:** `./mvnw test -Dtest=Task2And3QuestionAnswerTest` passes

<details>
<summary>Hint</summary>

`.call().chatClientResponse()`, then `response.context().get(...)`, cast to `List<Document>`.
</details>

## Task 4: Modular RAG

Implement `answerAdvanced(question)` with a `RetrievalAugmentationAdvisor`:

- a `RewriteQueryTransformer` (give it `chatClientBuilder.clone()`)
- a `VectorStoreDocumentRetriever` with `TOP_K`, `MIN_SIMILARITY`, and a filter for `type == policy`
- a `ContextualQueryAugmenter` that does **not** allow an empty context

**Done when:** `./mvnw test -Dtest=Task4ModularRagTest` passes

<details>
<summary>Hint</summary>

The retriever's filter takes a `Filter.Expression`:
`new FilterExpressionBuilder().eq("type", "policy").build()`
</details>

## Task 5: Let the assistant decide

In `BookstoreTools`, add a `searchShopPolicies(String question)` tool that returns
`policyAssistant.search(question)`. Write a description that tells the model **when** to use it.

**Done when:** `./mvnw test -Dtest=Task5AgenticRagTest` passes

## Task 6: Try it for real

1. `./mvnw test -Plive`
2. Try the curl commands in the README, and compare `/api/help` and `/api/help/advanced`
   on a messy question.
3. Through `/api/assistant/...` (module 06), ask about returns, then say "hi". When did
   the assistant search?

**Done when:** you've seen the assistant answer from the documents and cite them.

## Break it

- Change `withChunkSize(300)` to `50`, then to `2000`. Ask the same questions. What breaks?
- Add a new policy file, say `ebooks.md`, restart, and ask about it. No code changes needed.
  That's the power of RAG.
- Edit `returns.md` to say 14 days instead of 30, restart, and ask again.
- Ask "Can I return an e-book I downloaded yesterday?". The answer is in the documents, but
  in a different section than "Returning a printed book". Does retrieval find it?
