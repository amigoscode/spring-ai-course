# 11 · Evaluation

Start Docker before running the tests.
Run every test in the module with `./mvnw test`, or one task with the command under it.

Everything lives in the `eval` package. Read `policy-golden.json` first.

## Task 1: Fact checks that don't flake

Implement `FactCheck.normalise(text)` and `FactCheck.missing(answer, mustMention)`. Models write
"Don’t" with a curly apostrophe, add line breaks and change case. Normalise both sides, then
return the facts the answer doesn't mention.

**Done when:** `./mvnw test -Dtest=Task1FactCheckTest` passes (no Docker needed)

<details>
<summary>Hint</summary>

`text.toLowerCase(Locale.ROOT).replace('’', '\'')...replaceAll("\\s+", " ").strip()`
</details>

## Task 2: Measure retrieval

Implement `RetrievalEvaluator.evaluate(dataset, k)`. For each question, search the policies
(top `k`), find the rank of the expected source with `rankOf`, and return the hit rate, the MRR
and the questions that missed.

**Done when:** `./mvnw test -Dtest=Task2RetrievalEvalTest` passes. Look at the printed numbers.

<details>
<summary>Hint</summary>

A hit adds 1 to the hits and `1.0 / rank` to the reciprocal ranks. Divide both by the dataset size.
</details>

## Task 3: An LLM judge

Implement `LlmJudge.judge(question, answer, context)`: the `RUBRIC` as the system prompt, the
question, context and answer in the user message, and a `Judgement` back (structured output).

**Done when:** `./mvnw test -Dtest=Task3JudgeTest` passes

## Task 4: The eval runner

In `EvalRunner`:

1. `evaluate(golden)`: answer with `policyAssistant.answerWithSources`, then grade it three ways:
   the facts (`FactCheck.missing`), the source (is `expectedSource` in the sources?) and the judge
   (give it the context from `policyAssistant.search(question)`).
2. `run(dataset)`: evaluate every question and compute the pass rate.

**Done when:** `./mvnw test -Dtest=Task4EvalRunnerTest` passes

## Task 5: Run the real evaluation

1. `./mvnw test -Plive` runs the whole golden dataset against DeepSeek and prints a report.
2. Add 5 questions of your own to `policy-golden.json`, including one whose answer is **not** in
   the documents. What should `mustMention` be for that one?
3. Make a change and measure it: chunk size 50 instead of 300, or `TOP_K` 1 instead of 4, or a
   different system prompt in `PolicyAssistant`. Did the pass rate go up or down?

**Done when:** you've measured a change instead of guessing.

## Break it

- Make `PolicyAssistant`'s system prompt say "Always answer in Portuguese". Which checks catch it?
- Give the judge an answer that's correct but rude. What score does it get? Add a rule to the
  rubric and run it again.
- Use Spring AI's `RelevancyEvaluator` on a good answer. Read its source and explain why it can
  fail a perfect answer.
