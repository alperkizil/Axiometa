package com.axiometa.evaluation;

import com.axiometa.core.Candidate;
import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Evaluation;
import com.axiometa.core.Problem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Single-threaded {@link Evaluator} that evaluates candidates strictly in
 * input order on the calling thread.
 *
 * <p>Beyond the {@link Evaluator} contract, this implementation defensively
 * verifies the problem's own contract for every returned evaluation: a null
 * evaluation or an objective/constraint count that does not match the
 * problem's declarations becomes an {@link EvaluationOutcome.Failure}
 * carrying an {@link IllegalStateException}.
 *
 * <p>Instances have one owner and are not thread-safe.
 *
 * @param <R> the representation type
 */
public final class SequentialEvaluator<R> implements Evaluator<R> {

    private final Problem<R> problem;
    private long evaluationCount;

    /**
     * Creates an evaluator for one problem.
     *
     * @param problem the problem to evaluate against; must not be null
     * @throws NullPointerException if {@code problem} is null
     */
    public SequentialEvaluator(Problem<R> problem) {
        this.problem = Objects.requireNonNull(problem, "problem must not be null");
    }

    @Override
    public List<EvaluationOutcome<R>> evaluate(List<Candidate<R>> candidates) {
        Objects.requireNonNull(candidates, "candidates must not be null");
        for (int i = 0; i < candidates.size(); i++) {
            if (candidates.get(i) == null) {
                throw new NullPointerException("candidates[" + i + "] must not be null");
            }
        }
        List<EvaluationOutcome<R>> outcomes = new ArrayList<>(candidates.size());
        for (Candidate<R> candidate : candidates) {
            outcomes.add(evaluateOne(candidate));
        }
        return Collections.unmodifiableList(outcomes);
    }

    @Override
    public long evaluationCount() {
        return evaluationCount;
    }

    private EvaluationOutcome<R> evaluateOne(Candidate<R> candidate) {
        evaluationCount++;
        Evaluation evaluation;
        try {
            evaluation = problem.evaluate(candidate.representation());
        } catch (RuntimeException e) {
            return new EvaluationOutcome.Failure<>(candidate, e);
        }
        if (evaluation == null) {
            return contractFailure(candidate, "problem returned a null evaluation");
        }
        if (evaluation.objectiveCount() != problem.objectives().size()) {
            return contractFailure(candidate, "problem returned " + evaluation.objectiveCount()
                    + " objective values but declares " + problem.objectives().size()
                    + " objectives");
        }
        if (evaluation.constraintViolationCount() != problem.constraints().size()) {
            return contractFailure(candidate, "problem returned "
                    + evaluation.constraintViolationCount()
                    + " constraint violations but declares " + problem.constraints().size()
                    + " constraints");
        }
        return new EvaluationOutcome.Success<>(new EvaluatedCandidate<>(candidate, evaluation));
    }

    private EvaluationOutcome<R> contractFailure(Candidate<R> candidate, String message) {
        return new EvaluationOutcome.Failure<>(candidate, new IllegalStateException(message));
    }
}
