# 10 · Agent Patterns

"Agent" is the most overused word in AI. In practice, most reliable AI features are
**workflows**: a few model calls wired together with plain Java, in a shape you chose.
Anthropic's [Building effective agents](https://www.anthropic.com/engineering/building-effective-agents)
describes five shapes that cover most needs. You'll build all five for the bookstore.

**This module needs Docker running.**

## What you'll learn

| Pattern | When to use it | Here |
|---|---|---|
| **Chain** | A task splits into fixed steps | Newsletter: pick books → blurbs → newsletter |
| **Routing** | Different inputs need different handling | Support: classify → recommendation, policy or other |
| **Parallelization** | Independent pieces of work | Classify 50 reviews at once |
| **Orchestrator-workers** | The steps depend on the input | Reading plan: plan phases → one worker per phase → combine |
| **Evaluator-optimizer** | Quality has clear criteria | Blurb: write → judge → rewrite, at most 3 rounds |

## The idea

### Workflows vs agents

```
 WORKFLOW (you decide the steps)            AGENT (the model decides the steps)
 ───────────────────────────────            ───────────────────────────────────
 classify ─► route ─► answer                 loop:
                                               model picks a tool ─► run it ─► repeat
 predictable, testable, cheap               flexible, harder to test, cost varies
```

Module 06's assistant with tools was an agent: the model decided which tools to call and when.
This module is about workflows. Use the simplest shape that works, and add autonomy only
when the problem really needs it.

### Code is the glue

There's no special framework here. Each pattern is a normal Spring service with a few
`ChatClient` calls, structured output (module 03) to pass data between steps, and plain Java
for the control flow: `switch` for routing, virtual threads for parallel work, a `for` loop
with a limit for the evaluator.

### Every call costs

Count the calls: the chain is 3, the reading plan is 2 + one per phase, the evaluator up to 6.
Always put a limit on loops (`MAX_ROUNDS`), and watch `/api/usage` (module 05).

## Run it

```bash
./mvnw spring-boot:run
curl "localhost:8080/api/agents/newsletter?theme=writing+better+code"
curl -X POST -H "Content-Type: text/plain" localhost:8080/api/agents/support -d "Can I pay with PayPal?"
curl -X POST -H "Content-Type: application/json" localhost:8080/api/agents/reviews \
  -d '["Loved it", "Boring", "Arrived on time", "Best book ever", "Too expensive"]'
curl "localhost:8080/api/agents/reading-plan?goal=become+a+backend+engineer"
curl "localhost:8080/api/agents/blurb?title=Refactoring"
```

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Building effective agents (Anthropic)](https://www.anthropic.com/engineering/building-effective-agents)
- [Spring AI: agentic patterns](https://docs.spring.io/spring-ai/reference/api/effective-agents.html)
