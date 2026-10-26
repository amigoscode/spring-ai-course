package com.amigoscode.bookstore.policy;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Answers questions about shipping, returns, payments and the loyalty programme,
 * from the shop's own documents instead of the model's imagination.
 */
@Service
public class PolicyAssistant {

	static final int TOP_K = 4;

	static final double MIN_SIMILARITY = 0.3;

	private final ChatClient chatClient;

	private final ChatClient.Builder chatClientBuilder;

	private final VectorStore vectorStore;

	public PolicyAssistant(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
		this.chatClientBuilder = chatClientBuilder.clone();
		this.vectorStore = vectorStore;
		this.chatClient = chatClientBuilder
			.defaultSystem("You are the customer support assistant of Amigos Books. Answer in at most three sentences.")
			.defaultOptions(ChatOptions.builder().temperature(0.0))
			.build();
	}

	/**
	 * Naive RAG: search, stuff the results into the prompt, answer.
	 */
	public String answer(String question) {
		return chatClient.prompt()
			.user(question)
			.advisors(questionAnswerAdvisor())
			.call()
			.content();
	}

	/**
	 * Like {@link #answer}, plus the documents the answer was based on, so the user can check it.
	 * The same question gets the same answer for an hour, without paying for another model call.
	 */
	@Cacheable(cacheNames = "policy-answers", key = "#question.strip().toLowerCase()")
	public AnswerWithSources answerWithSources(String question) {
		ChatClientResponse response = chatClient.prompt()
			.user(question)
			.advisors(questionAnswerAdvisor())
			.call()
			.chatClientResponse();

		@SuppressWarnings("unchecked")
		List<Document> documents = (List<Document>) response.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
		List<String> sources = documents.stream()
			.map(document -> (String) document.getMetadata().get("source"))
			.distinct()
			.toList();
		return new AnswerWithSources(response.chatResponse().getResult().getOutput().getText(), sources);
	}

	/**
	 * Modular RAG: rewrite the question for search, retrieve, then augment the prompt.
	 * If nothing relevant is found, the model is told to say it can't answer.
	 */
	public String answerAdvanced(String question) {
		var advisor = RetrievalAugmentationAdvisor.builder()
			.queryTransformers(RewriteQueryTransformer.builder().chatClientBuilder(chatClientBuilder.clone()).build())
			.documentRetriever(VectorStoreDocumentRetriever.builder()
				.vectorStore(vectorStore)
				.topK(TOP_K)
				.similarityThreshold(MIN_SIMILARITY)
				.filterExpression(new FilterExpressionBuilder().eq("type", "policy").build())
				.build())
			.queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(false).build())
			.build();

		return chatClient.prompt().user(question).advisors(advisor).call().content();
	}

	/**
	 * The raw search, used by the assistant's tool.
	 */
	public List<String> search(String question) {
		return vectorStore.similaritySearch(policySearch(question)).stream().map(Document::getText).toList();
	}

	private QuestionAnswerAdvisor questionAnswerAdvisor() {
		return QuestionAnswerAdvisor.builder(vectorStore).searchRequest(policySearch(null)).build();
	}

	private static SearchRequest policySearch(String query) {
		var builder = SearchRequest.builder()
			.topK(TOP_K)
			.similarityThreshold(MIN_SIMILARITY)
			.filterExpression("type == 'policy'");
		return query == null ? builder.build() : builder.query(query).build();
	}

	public record AnswerWithSources(String answer, List<String> sources) {
	}

}
