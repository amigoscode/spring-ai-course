package com.amigoscode.bookstore.eval;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

/**
 * LLM-as-a-judge: a second model call grades the answer with a rubric. It catches what
 * keyword checks can't (a correct fact in a misleading answer), but it's a model too:
 * temperature 0, a precise rubric, and check it against your own judgement now and then.
 */
@Service
public class LlmJudge {

	static final String RUBRIC = """
			You grade answers of a bookstore's support assistant.
			Score from 1 to 5:
			5: correct, complete, and only uses facts from the context
			4: correct, but misses a useful detail
			3: partly correct
			2: mostly wrong or unhelpful
			1: wrong, or states facts that are not in the context
			"grounded" is true only if every fact in the answer is supported by the context.""";

	private final ChatClient chatClient;

	public LlmJudge(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.clone().defaultOptions(ChatOptions.builder().temperature(0.0)).build();
	}

	public Judgement judge(String question, String answer, String context) {
		// SOLUTION-START task-3
		return chatClient.prompt()
			.system(RUBRIC)
			.user(u -> u.text("""
					Question: {question}

					Context:
					{context}

					Answer to grade:
					{answer}""").param("question", question).param("context", context).param("answer", answer))
			.call()
			.entity(Judgement.class);
		// SOLUTION-ELSE
		// TASK: // TODO task-3: send the RUBRIC as the system prompt, and the question, context and answer
		// TASK: // as the user message. Return the grade as a Judgement (structured output).
		// TASK: throw new UnsupportedOperationException("TODO task-3");
		// SOLUTION-END
	}

	public record Judgement(int score, boolean grounded, String reasoning) {

		public boolean passes() {
			return grounded && score >= 4;
		}

	}

}
