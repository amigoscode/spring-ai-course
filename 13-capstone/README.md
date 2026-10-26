# 13 · Capstone: Gift Finder

No step-by-step instructions this time. You get a feature brief, a contract and acceptance
tests, like a ticket at work. The design is yours. Everything from modules 00 to 12 is in this
project and ready to use.

**This module needs Docker running.**

## The brief

> Customers often buy books as gifts for developer friends, and they ask support "what should
> I get my nephew who just started a Java bootcamp?". Build a **Gift Finder**: the customer
> describes the person, the occasion and a budget, and gets up to 3 books with a reason for each.
>
> The shop loses money and trust if the Gift Finder suggests a book we don't sell, one that's
> out of stock, or goes over the budget. That must never happen, whatever the model says.

## The contract

```
POST /api/gifts            (logged-in customers only)
{
  "recipient": "My nephew, he just started a Java bootcamp",   required, at most 500 characters
  "occasion":  "birthday",                                     optional, at most 100 characters
  "budget":    50.00                                           required, from 10 to 500
}

200 OK
{
  "suggestions": [
    {"title": "Effective Java", "author": "Joshua Bloch", "price": 42.00, "reason": "..."}
  ],
  "total": 42.00,
  "message": "A great first step after a bootcamp..."
}
```

`GiftRequest` and `GiftSuggestions` are already written. Don't change them: the tests use them.

## The rules (they must hold 100% of the time)

1. At most 3 suggestions, no duplicates.
2. Every book is in the catalog, **in stock**, with the catalog's price and author.
3. `total` is the sum of the prices, and never more than the budget.
4. Every suggestion has a reason, and there's always a message for the customer.
5. If nothing in stock fits the budget: no suggestions, a helpful message, and **no model call**.
6. Invalid requests are rejected with 400 before any model call.
7. Only logged-in customers can use it, and it counts toward their rate limit.

And one rule about quality: for the golden dataset in `src/main/resources/eval/gift-golden.json`,
at least 80% of the answers must include one of the expected books.

## How you'll know you're done

```bash
./mvnw test           # the offline acceptance tests: rules 5, 6 and 7
./mvnw test -Plive    # the live acceptance test: rules 1 to 4 on every golden answer, and quality
```

`GiftRules` in the tests checks rules 1 to 4. Read it.

## Things to think about

There's no single right design. Some questions worth answering before you write code:

- Where does stock and price information come from: the prompt, a tool, or your own code after
  the model answers? Which of these can the model get wrong?
- Do you show the model every book, or only the ones it's allowed to pick?
- What do you do when the model suggests something that breaks a rule: drop it, replace it, or ask again?
- How many model calls does one request cost? Can you make it one?
- How do you test it without paying for DeepSeek every time?

A reference solution is on the `solution` branch. Build yours first, then compare.

## Your turn

Open [TASKS.md](TASKS.md).
