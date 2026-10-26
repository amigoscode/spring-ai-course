# 09 · MCP

Start Docker before running the tests. The tests start the app on a random port and connect
a real MCP client to it.
Run every test in the module with `./mvnw test`, or one task with the command under it.

## Task 1: Publish the read-only tools

In `PublicCatalogTools`, publish `checkStock`, `findBooksByTopic` and `searchShopPolicies` as
MCP tools. Each one calls the same method on `BookstoreTools`. Use `@McpTool` (with that exact
`name`) and `@McpToolParam`, and mark them read-only.

`placeOrder` and `orderStatus` must **not** be published. Why not?

**Done when:** `./mvnw test -Dtest=Task1And2McpServerTest#task1*` passes

<details>
<summary>Hint</summary>

```java
@McpTool(name = "checkStock", description = "...",
         annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
public BookstoreTools.StockInfo checkStock(@McpToolParam(description = "...") String title) {
    return tools.checkStock(title);
}
```
</details>

## Task 2: Introduce the server

In `application.properties`, give the MCP server the name `amigos-books`, the version `1.0.0`,
and instructions that tell clients what it's for (they must mention "Amigos Books").

**Done when:** `./mvnw test -Dtest=Task1And2McpServerTest` passes

<details>
<summary>Hint</summary>

`spring.ai.mcp.server.name`, `spring.ai.mcp.server.version` and `spring.ai.mcp.server.instructions`.
</details>

## Task 3: Publish the policies as resources

In `PolicyResources`, publish each `docs/<name>.md` file as the resource `policy://<name>`
with `@McpResource`. The name ends up in a file path, so keep only letters: a client must not
be able to read `../../application.properties`.

**Done when:** `./mvnw test -Dtest=Task3McpResourceTest` passes

<details>
<summary>Hint</summary>

`@McpResource(uri = "policy://{name}", ...)` on a method with a `String name` parameter.
Read the file with `new ClassPathResource("docs/" + safeName + ".md").getContentAsString(UTF_8)`.
</details>

## Task 4: Connect a real client

1. Start the app with `./mvnw spring-boot:run`.
2. Open the MCP Inspector (`npx @modelcontextprotocol/inspector`), connect to
   `http://localhost:8080/mcp` with Streamable HTTP, list the tools and call `checkStock`.
3. If you use Claude Code: `claude mcp add --transport http amigos-books http://localhost:8080/mcp`
   and ask it about the shop's stock and return policy.

**Done when:** an AI app you didn't write has used your tools.

## Task 5 (bonus): Be a client

Run the app with the `deepwiki` profile (see the README) and ask `/api/repo-expert` about a
GitHub repository you know. Read `RepoExpert` and `application-deepwiki.properties`.

**Done when:** your app has used a tool from someone else's MCP server.

## Break it

- Publish `placeOrder` over MCP too. What customer id does it get? Who could call it?
- Ask for `policy://..%2F..%2Fapplication`. Remove the letters-only check and try again.
- Give `checkStock` a misleading description, like "Deletes a book". Does Claude Code still
  call it? Does it ask you first? (This is why tool descriptions from servers you don't trust
  are a risk: look up "tool poisoning".)
