# 13 · Capstone: Gift Finder

Read the brief in the [README](README.md) first. These are milestones, not instructions.
Each one is done when its acceptance criteria are met.

## Task 1: Design it

Write down, in a few lines (a comment at the top of `GiftFinder` is fine):

- which data the model sees, and which decisions your code makes instead
- how many model calls one request costs
- how you'll make sure the 7 rules hold even when the model is wrong

**Done when:** you can explain your design to someone in two minutes.

## Task 2: The endpoint

`POST /api/gifts` in `GiftController`: logged-in customers only, a validated `GiftRequest`,
`GiftSuggestions` back. Invalid requests get a 400 and never reach the model.

**Done when:** `./mvnw test -Dtest=CapstoneAcceptanceTest` passes, except
`whenNothingFitsTheBudgetTheModelIsNotCalled`

## Task 3: The Gift Finder

Implement `GiftFinder.find(request)` so that all 7 rules hold.

**Done when:** `./mvnw test` passes, and `./mvnw test -Plive` passes rules 1 to 4 for every golden answer

## Task 4: Make it good

Improve the suggestions until at least 80% of the golden dataset gets one of the expected books.
Measure, change one thing, measure again (module 11).

**Done when:** `./mvnw test -Plive` passes

## Task 5: Ship it

Pick at least two:

- Add 5 recipients of your own to `gift-golden.json`, including a tricky one (tiny budget, a
  topic the shop doesn't cover, a recipient who "already owns Clean Code").
- Let the customer refine the result in a conversation: "something cheaper", "not that one".
- Expose the Gift Finder as an MCP tool, and use it from Claude Code.
- Stream the message to the browser while the suggestions are prepared.
- Add a metric for how often your code had to drop a suggestion the model made.
- Let the customer order a suggested book in one step, safely.

**Done when:** you've shipped something you'd be happy to demo.
