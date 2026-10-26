# 09 · MCP

Your assistant has tools, but only your app can use them. The **Model Context Protocol (MCP)**
is a standard way to publish tools and documents so that *any* AI app can use them: Claude
Desktop, Claude Code, Cursor, or another Spring AI app. Think of it as USB for AI tools.

**This module needs Docker running.**

## What you'll learn

- What MCP is: servers, clients, tools, resources
- Turning the bookstore into an MCP server with `@McpTool` and `@McpResource`
- Deciding what to expose (and what never to)
- Connecting Claude Code, Claude Desktop and the MCP Inspector to it
- Bonus: the other direction, your app using someone else's MCP server

## The idea

### One protocol, many clients

```
 Claude Desktop ─┐                                ┌─ checkStock          (tool)
 Claude Code ────┤   MCP over HTTP                ├─ findBooksByTopic    (tool)
 Cursor ─────────┼──────────────────►  /mcp  ─────┼─ searchShopPolicies  (tool)
 Another app ────┘   (Streamable HTTP)            └─ policy://{name}     (resource)
                                        Amigos Books
```

An MCP server offers:

- **Tools**: actions the model can call (like module 06, but for any client)
- **Resources**: documents a client can read or attach to a conversation
- **Prompts**: reusable prompt templates (not used here)

### Expose with care

In module 06, `placeOrder` was safe because **your app** knew who the customer was and put it
in the `ToolContext`. An MCP client is anybody on the network. There's no logged-in customer.
So the MCP server publishes an explicit allow-list of **read-only** tools, in its own class
(`PublicCatalogTools`). Adding a tool to MCP should be a deliberate decision, not a side effect
of writing `@Tool`.

Real MCP servers that act for a user need authentication (MCP supports OAuth 2.1). That's
beyond this module.

### Annotations

```java
@McpTool(name = "checkStock", description = "...",
         annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
public StockInfo checkStock(@McpToolParam(description = "...") String title) { ... }

@McpResource(uri = "policy://{name}", name = "Shop policy", mimeType = "text/markdown")
public String policy(String name) { ... }
```

The hints tell clients the tool is safe to call without asking the user each time.

### Use it from Claude Code

With the app running:

```bash
claude mcp add --transport http amigos-books http://localhost:8080/mcp
```

Then ask Claude Code "Is Clean Code in stock at Amigos Books?".

Or inspect it without any AI at all:

```bash
npx @modelcontextprotocol/inspector
```

Choose **Streamable HTTP** with the URL `http://localhost:8080/mcp`, connect, and call the tools by hand.

### Bonus: the other direction

The app can also be an MCP **client**. Run it with the `deepwiki` profile and it connects to
DeepWiki's public MCP server, which answers questions about any GitHub repository:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=deepwiki
curl "localhost:8080/api/repo-expert?q=In+spring-projects/spring-ai,+what+does+QuestionAnswerAdvisor+do"
```

See `application-deepwiki.properties` and `RepoExpert`: a few lines of configuration and the
remote tools work like local ones.

## Your turn

Open [TASKS.md](TASKS.md).

## Docs

- [Model Context Protocol](https://modelcontextprotocol.io)
- [Spring AI MCP server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [Spring AI MCP annotations](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-overview.html)
