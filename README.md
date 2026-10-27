# Spring AI Course

Learn to build AI applications with **Spring Boot 4** and **Spring AI 2**, one exercise at a time.

You build one app all the way through: the assistant of **Amigos Books**, an online bookstore
for developers. Every module adds one capability: prompts, memory, tools, RAG, MCP, agents,
evaluation, production. Each module is a standalone Spring Boot project with a README that
explains the idea, a `TASKS.md` with what to build, and tests that turn green when you're done.

## Modules

| # | Module | You learn | Docker |
|---|---|---|---|
| 00 | [First Chat](00-first-chat) | `ChatClient`, DeepSeek config, tokens | No |
| 01 | [ChatClient in Depth](01-chat-client) | Message roles, options, streaming, no memory | No |
| 02 | [Prompt Templates](02-prompt-templates) | `.st` templates, prompt stuffing, few-shot | No |
| 03 | [Structured Output](03-structured-output) | `.entity(...)`, lists, retries, validating model output | No |
| 04 | [Chat Memory](04-chat-memory) | `MessageChatMemoryAdvisor`, windows, memory in Postgres | Yes |
| 05 | [Advisors](05-advisors) | Custom advisors, token tracking, guardrails | Yes |
| 06 | [Tool Calling](06-tool-calling) | `@Tool`, the tool loop, `ToolContext`, authorization | Yes |
| 07 | [Embeddings](07-embeddings) | Vectors, cosine similarity, local ONNX, pgvector | Yes |
| 08 | [RAG](08-rag) | ETL, `QuestionAnswerAdvisor`, modular RAG, sources | Yes |
| 09 | [MCP](09-mcp) | MCP server with tools and resources, MCP client | Yes |
| 10 | [Agent Patterns](10-agent-patterns) | Chain, routing, parallel, orchestrator, evaluator | Yes |
| 11 | [Evaluation](11-evaluation) | Golden datasets, retrieval metrics, LLM judge | Yes |
| 12 | [Production](12-production) | Spring Security, resilience, rate limits, caching, metrics | Yes |
| 13 | [Capstone: Gift Finder](13-capstone) | A feature brief and acceptance tests, your design | Yes |

## Stack

- Java 25, Spring Boot 4.1, Spring AI 2.0
- [DeepSeek](https://platform.deepseek.com) for chat
- Local ONNX embeddings and Postgres with pgvector (from module 07)
- The MCP Java SDK (module 09), Spring Security and Actuator (module 12)
- Docker Compose for the modules that need a database (from module 04)

## Get started

### 1. Fork this repo

Click **Fork** at the top of the page, then clone **your fork**:

```bash
git clone https://github.com/<your-username>/spring-ai-course.git
cd spring-ai-course
git remote add upstream https://github.com/amigoscode/spring-ai-course.git
```

### 2. Get a DeepSeek API key

1. Create a key at [platform.deepseek.com/api_keys](https://platform.deepseek.com/api_keys)
2. Add a few dollars of credit. The whole course costs well under $1 in API calls.
3. Copy `.env.example` to `.env` in the repo root and paste your key:

```bash
cp .env.example .env
```

`.env` is in `.gitignore`. Never commit your key. Every module reads the key from this one
file, or from a `DEEPSEEK_API_KEY` environment variable if you prefer.

### 3. Set up your task list

1. In your fork, go to **Settings → General → Features** and tick **Issues**.
2. Go to the **Actions** tab and click **I understand my workflows, go ahead and enable them**.
3. Click **Set up my tasks** on the left, then **Run workflow**.

You get one issue per task and a milestone per module with a progress bar. Close the issues
as you go, or write `closes #12` in a commit message.

<details>
<summary>Optional: a kanban board</summary>

```bash
gh auth refresh -s project
./scripts/create_board.sh
```

It creates a GitHub Projects board with all your task issues. Switch its layout to **Board**
to drag tasks from Todo to Done.
</details>

### Windows

Everything works on Windows. Install [Git for Windows](https://git-scm.com/download/win)
and a [Java 25 JDK](https://adoptium.net/temurin/releases/?version=25). You don't need
to install Maven: every module has a Maven wrapper (`mvnw` for macOS and Linux,
`mvnw.cmd` for Windows).

| macOS / Linux | Windows PowerShell | Windows cmd |
|---|---|---|
| `./mvnw test` | `.\mvnw test` | `mvnw test` |
| `./mvnw test -Dtest=Task3RecommendTest` | `.\mvnw test "-Dtest=Task3RecommendTest"` | `mvnw test -Dtest=Task3RecommendTest` |
| `cp .env.example .env` | `Copy-Item .env.example .env` | `copy .env.example .env` |
| `curl "localhost:8080/..."` | `curl.exe "localhost:8080/..."` | `curl "localhost:8080/..."` |

- In PowerShell, put `-D...` arguments in quotes, otherwise PowerShell can split them.
- In Windows PowerShell 5, `curl` is an alias for `Invoke-WebRequest`. Type `curl.exe` instead.
- Modules from 04 onward need [Docker Desktop](https://www.docker.com/products/docker-desktop/) running.
- The optional `scripts/create_board.sh` runs in **Git Bash**, which comes with Git for Windows.

The commands in each module's README and TASKS.md use the macOS/Linux form. Swap `./mvnw`
for `.\mvnw` (PowerShell) or `mvnw` (cmd).

### 4. Start module 00

Open `00-first-chat` in your IDE (open the module folder, not the repo root) and read its README.

## How each module works

```
04-chat-memory/
├── README.md       the idea, with diagrams. Read it first.
├── TASKS.md        what to build, step by step
├── compose.yaml    the database, if the module needs one
├── pom.xml         a standalone Spring Boot project
└── src/
    ├── main/       starter code, with // TODO task-N comments
    └── test/       one test class per task, failing until you finish it
```

```bash
./mvnw test                         # all tasks in the module
./mvnw test -Dtest=Task3RecommendTest  # one task
./mvnw test -Plive                  # tests that call the real DeepSeek API
./mvnw spring-boot:run              # run the app
```

The task tests use a **fake model**: they're instant, free, and work offline. The `live`
tests call DeepSeek for real and cost a fraction of a cent.

From module 04 on, **Docker must be running**: the app starts Postgres with Docker Compose, and
the tests start their own with Testcontainers. Module 07 also downloads a small embedding model
(about 90 MB) the first time.

Each module starts where the previous one ended, so you can jump into any module
even if you skipped one.

## Stuck? Compare with the solution

The finished code is on the `solution` branch. To see the answer for one module:

```bash
git fetch upstream solution
git diff main upstream/solution -- 02-prompt-templates
```

Try for a while first. Each task also has a hint under it in `TASKS.md`.

## Getting updates

New modules and fixes land on `main`. To get them:

1. Commit your work first: `git add -A && git commit -m "my progress"`
2. Pull the updates:

```bash
git fetch upstream
git merge upstream/main
```

If you use the **Sync fork** button on GitHub instead and it shows a conflict, **never click
"Discard commits"**. That deletes your work. Use the commands above and resolve the conflict.

See [CHANGELOG.md](CHANGELOG.md) for what changed.
