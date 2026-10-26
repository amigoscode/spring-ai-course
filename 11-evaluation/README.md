# 11 · Evaluation

You change a prompt. Did the assistant get better or worse? With normal code you run the
tests. With AI the answer is different every time, so `assertEquals` doesn't work. This
module is about testing what can't be tested exactly. It's the skill that separates a demo
from a product.

**This module needs Docker running.**

## What you'll learn

- Golden datasets: questions with known good answers
- Three layers of checks, from cheap and certain to expensive and fuzzy
- Measuring retrieval on its own: hit rate and MRR
- LLM-as-a-judge with a rubric, and its limits
- An eval suite you run on every change, with a pass rate

## The idea

### Three layers

```
 cheap, deterministic, never flaky                         expensive, fuzzy, catches more
 ─────────────────────────────────────────────────────────────────────────────────────────►
 1. Fact checks                 2. Retrieval checks             3. LLM judge
    "does it say 30 days?"         "did search find              "is it correct, complete
    plain string checks            returns.md?"                   and grounded? score 1-5"
    free                           free (no LLM)                  one extra model call
```

Run the cheap ones first. Only reach for a judge for what string checks can't see: an answer
that says "30 days" but gets the rest wrong.

### Golden dataset

`src/main/resources/eval/policy-golden.json` holds questions with the document that answers
them and the facts the answer must mention. It's small (10 questions) on purpose. Real teams
start with 20 to 50 and add every bug a user reports.

```json
{"question": "How much does express delivery cost?", "expectedSource": "shipping.md", "mustMention": ["9.90"]}
```

### Retrieval: hit rate and MRR

If search misses the right document, no model can answer well. So measure search on its own:

- **Hit rate @k**: how often the right document is in the top k results.
- **MRR (mean reciprocal rank)**: 1 if it's first, ½ if second, ⅓ if third, 0 if missing,
  averaged. It rewards finding it near the top.

These need no LLM, so you can try 20 chunk sizes in a minute.

### The judge is a model too

An LLM judge can be wrong, can be fooled by confident answers, and can be lenient. Make it
reliable: temperature 0, a precise rubric, structured output, and spot-check its grades by
hand. Spring AI ships `RelevancyEvaluator` and `FactCheckingEvaluator`. Read their source:
they pass only if the model answers exactly "yes", so "Yes." with a full stop fails. That's
why this module uses a structured `Judgement` instead.

### When to run it

Whenever you change a prompt, the chunking, the model, the retrieval settings or the
documents. Compare the pass rate before and after. A drop is a regression, like a failing test.

## Run it

```bash
./mvnw test -Plive          # the full eval against DeepSeek, prints a report
```

```
PASS  How long do I have to return a printed book?   facts:ok source:ok judge:5
PASS  How much does express delivery cost?           facts:ok source:ok judge:5
...
pass rate: 100%
```

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Spring AI evaluation testing](https://docs.spring.io/spring-ai/reference/api/testing.html)
- [A field guide to rapidly improving AI products (Hamel Husain)](https://hamel.dev/blog/posts/field-guide/)
