# Maintaining the course

**Only edit the `solution` branch.** `main` is generated from it by
`.github/workflows/publish-tasks.yml` on every push to `solution`. Never commit to `main` by hand.

## Marking the answer

Wrap solution code in markers, using the file's comment style (`//`, `#` or `<!-- -->`):

```java
// TODO task-3: send the question as a user message and return the answer text
throw new UnsupportedOperationException("TODO task-3");
```

- Lines between `SOLUTION-START` and `SOLUTION-ELSE` are removed from `main`.
- `TASK:` lines become the starter code, with the `// TASK: ` prefix stripped.
  The starter code must compile.
- Files the student creates from scratch go in `.solution-only`.

## Checklist for a new module

1. Copy the previous module's **finished** code into a new folder (`NN-name`).
   Remove its markers, keeping the solution.
2. Add the new tasks with markers, and one `TaskNXxxTest` class per task.
3. Write `README.md` (the idea) and `TASKS.md` (`## Task N: Title` sections, a
   **Done when** line with the test command, a hint in `<details>`).
4. Check it locally:

```bash
(cd NN-name && ./mvnw verify)                     # solution passes
python3 scripts/generate_tasks.py . /tmp/tasks    # generate the student version
(cd /tmp/tasks/NN-name && ./mvnw test)            # must compile, and fail
```

5. Add the module to the table in `README.md` and to `CHANGELOG.md`.

## Rules that keep student forks mergeable

- Prefer adding new modules over changing released ones.
- Fix bugs in released modules with small, targeted changes, and list them in `CHANGELOG.md`.
- Never rename or move a released module's files.
