# 10 · Agent Patterns

Start Docker before running the tests.
Run every test in the module with `./mvnw test`, or one task with the command under it.

Each pattern lives in its own class in `agents/`. Read the comment at the top of each one first.

## Task 1: Chain

Implement `NewsletterChain.write(theme)` as three calls, each using the previous result:

1. Pick 3 catalog titles for the theme, as a `List<String>` (structured output).
2. Write a two sentence blurb for each title.
3. Write a short newsletter around those blurbs.

**Done when:** `./mvnw test -Dtest=Task1ChainTest` passes

<details>
<summary>Hint</summary>

Step 1 is `.entity(new ParameterizedTypeReference<List<String>>() {})`. Include
`catalog.asPromptText()` in that prompt so the model picks real books.
</details>

## Task 2: Routing

Implement `SupportRouter.handle(message)`:

1. Classify the message into a `RouteDecision` (structured output, temperature 0). Describe the
   three routes in the system prompt.
2. `RECOMMENDATION` → `bookAssistant.recommend(message, 3)`, `POLICY` → `policyAssistant.answer(message)`,
   `OTHER` → `OTHER_ANSWER` **without** another model call.

**Done when:** `./mvnw test -Dtest=Task2RouterTest` passes

<details>
<summary>Hint</summary>

A `switch` on `decision.route()` that returns the answer.
</details>

## Task 3: Parallelization

Implement `ReviewAnalyzer.summarize(reviews)`: classify every review with
`bookAssistant.classify(review)` **at the same time**, then count how many got each `Sentiment`.

**Done when:** `./mvnw test -Dtest=Task3ParallelTest` passes (the test model takes 200ms per
call, so a sequential version takes 2 seconds and fails)

<details>
<summary>Hint</summary>

`try (var executor = Executors.newVirtualThreadPerTaskExecutor())`, submit one task per review,
then `future.get()` each one and `Collectors.groupingBy(Function.identity(), Collectors.counting())`.
</details>

## Task 4: Orchestrator-workers

Implement `ReadingPlanner.plan(goal)`:

1. **Orchestrator**: split the goal into a `Plan` of 2 to 4 phases (structured output).
2. **Workers**: one call per phase that picks 1 or 2 catalog books for it.
3. **Synthesizer**: one call that combines the worker answers into a reading plan.

**Done when:** `./mvnw test -Dtest=Task4OrchestratorTest` passes

## Task 5: Evaluator-optimizer

Implement `BlurbOptimizer.write(title)`. Up to `MAX_ROUNDS` times:

1. The **writer** writes a blurb, given the criteria, its previous attempt and the feedback.
2. The **evaluator** returns an `Evaluation` (structured output).
3. If the verdict is `PASS`, return the blurb, the round and `accepted = true`.

If no round passes, return the last blurb with `accepted = false`.

**Done when:** `./mvnw test -Dtest=Task5EvaluatorOptimizerTest` passes

<details>
<summary>Hint</summary>

Lambdas need effectively final variables. Copy `blurb` and `feedback` into new local variables
before you use them inside `.user(u -> ...)`.
</details>

## Task 6: Try it for real

1. `./mvnw test -Plive`
2. Try every curl command in the README. Watch the logs: how many model calls did each one make?
3. Send `/api/agents/reviews` 30 reviews. How long does it take? How long would it take one by one?

**Done when:** you can say which pattern you'd use for a new feature, and why.

## Break it

- In the router, ask "My order hasn't arrived and I want a refund, also recommend a book". One
  route can't handle it. How would you change the design?
- Make the evaluator's criteria impossible ("at most 3 words, and explain the whole book").
  What does `MAX_ROUNDS` save you from?
- In the orchestrator, give a goal with nothing to do with books. What does the plan look like?
