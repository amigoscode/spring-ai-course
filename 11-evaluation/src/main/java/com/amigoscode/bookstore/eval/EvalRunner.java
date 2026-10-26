package com.amigoscode.bookstore.eval;

import java.util.List;
import java.util.stream.Collectors;

import com.amigoscode.bookstore.policy.PolicyAssistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs the whole golden dataset through the real pipeline and grades every answer three ways.
 * Run it whenever you change a prompt, the chunking, the model or the retrieval settings.
 */
@Service
public class EvalRunner {

	private static final Logger log = LoggerFactory.getLogger(EvalRunner.class);

	private final PolicyAssistant policyAssistant;

	private final LlmJudge judge;

	public EvalRunner(PolicyAssistant policyAssistant, LlmJudge judge) {
		this.policyAssistant = policyAssistant;
		this.judge = judge;
	}

	public EvalReport run(List<GoldenQuestion> dataset) {
		// SOLUTION-START task-4
		List<EvalResult> results = dataset.stream().map(this::evaluate).toList();
		long passed = results.stream().filter(EvalResult::passed).count();
		EvalReport report = new EvalReport(results, (double) passed / results.size());
		log.info("\n{}", report.table());
		return report;
		// SOLUTION-ELSE
		// TASK: // TODO task-4: evaluate every question, and return the results with the share that passed
		// TASK: throw new UnsupportedOperationException("TODO task-4");
		// SOLUTION-END
	}

	EvalResult evaluate(GoldenQuestion golden) {
		// SOLUTION-START task-4
		var answer = policyAssistant.answerWithSources(golden.question());
		List<String> missing = FactCheck.missing(answer.answer(), golden.mustMention());
		boolean rightSource = answer.sources().contains(golden.expectedSource());
		String context = String.join("\n\n", policyAssistant.search(golden.question()));
		LlmJudge.Judgement judgement = judge.judge(golden.question(), answer.answer(), context);
		return new EvalResult(golden.question(), answer.answer(), missing, rightSource, judgement);
		// SOLUTION-ELSE
		// TASK: // TODO task-4: answer the question with policyAssistant.answerWithSources, then check it three ways:
		// TASK: //   the facts (FactCheck.missing), the source (expectedSource is in the sources),
		// TASK: //   and the judge (give it the context from policyAssistant.search)
		// TASK: throw new UnsupportedOperationException("TODO task-4");
		// SOLUTION-END
	}

	public record EvalResult(String question, String answer, List<String> missingFacts, boolean rightSource,
			LlmJudge.Judgement judgement) {

		public boolean passed() {
			return missingFacts.isEmpty() && rightSource && judgement.passes();
		}

	}

	public record EvalReport(List<EvalResult> results, double passRate) {

		public String table() {
			return results.stream()
				.map(r -> "%s  %-55s facts:%s source:%s judge:%d%s".formatted(r.passed() ? "PASS" : "FAIL",
						r.question(), r.missingFacts().isEmpty() ? "ok" : "missing " + r.missingFacts(),
						r.rightSource() ? "ok" : "wrong", r.judgement().score(),
						r.judgement().grounded() ? "" : " (not grounded)"))
				.collect(Collectors.joining("\n", "", "\npass rate: %.0f%%".formatted(passRate * 100)));
		}

	}

}
