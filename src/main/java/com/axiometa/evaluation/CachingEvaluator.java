package com.axiometa.evaluation;

import com.axiometa.core.Candidate;
import com.axiometa.core.EvaluatedCandidate;
import com.axiometa.core.Evaluation;
import com.axiometa.core.EvaluationSemantics;
import com.axiometa.core.Problem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Decorator that caches successful evaluations of a deterministic problem,
 * keyed by representation value — never by candidate identity.
 *
 * <p>Semantics:
 *
 * <ul>
 *   <li>For a {@link EvaluationSemantics#STOCHASTIC} problem the cache is
 *       bypassed entirely — no reads, no writes, no hits. Caching a
 *       stochastic problem would silently make it deterministic.</li>
 *   <li>For a {@link EvaluationSemantics#DETERMINISTIC} problem, requests
 *       whose representation is already cached are served as
 *       {@link EvaluationOutcome.Success} pairing the requesting candidate
 *       with the cached evaluation; all other requests are forwarded to the
 *       wrapped evaluator as one batch, preserving batching for parallel
 *       evaluators. Within one batch, duplicates of an uncached
 *       representation are all forwarded; caching takes effect from the next
 *       batch.</li>
 *   <li>Only successes are cached. Failures are never cached; a later
 *       request for the same representation is forwarded again.</li>
 *   <li>The cache is unbounded and never evicts: memory grows with the
 *       number of distinct representations evaluated.</li>
 *   <li>{@link #evaluationCount()} reports the wrapped evaluator's count, so
 *       the top of a decorator stack always reports real evaluation
 *       attempts. {@link #cacheHitCount()} counts requests served from the
 *       cache.</li>
 * </ul>
 *
 * <p>Instances have one owner and are not thread-safe.
 *
 * @param <R> the representation type
 */
public final class CachingEvaluator<R> implements Evaluator<R> {

    private final Evaluator<R> inner;
    private final EvaluationSemantics semantics;
    private final Map<R, Evaluation> cache = new HashMap<>();
    private long cacheHitCount;

    /**
     * Creates a caching decorator around an evaluator.
     *
     * @param inner   the evaluator to wrap; must not be null
     * @param problem the problem {@code inner} evaluates against; must not be
     *                null; only its {@link Problem#evaluationSemantics()} is
     *                consulted, once, at construction
     * @throws NullPointerException if {@code inner}, {@code problem}, or the
     *                              problem's declared semantics is null
     */
    public CachingEvaluator(Evaluator<R> inner, Problem<R> problem) {
        this.inner = Objects.requireNonNull(inner, "inner must not be null");
        Objects.requireNonNull(problem, "problem must not be null");
        this.semantics = Objects.requireNonNull(problem.evaluationSemantics(),
                "problem.evaluationSemantics() must not be null");
    }

    @Override
    public List<EvaluationOutcome<R>> evaluate(List<Candidate<R>> candidates) {
        Objects.requireNonNull(candidates, "candidates must not be null");
        for (int i = 0; i < candidates.size(); i++) {
            if (candidates.get(i) == null) {
                throw new NullPointerException("candidates[" + i + "] must not be null");
            }
        }
        if (semantics == EvaluationSemantics.STOCHASTIC) {
            return inner.evaluate(candidates);
        }
        List<Candidate<R>> batch = List.copyOf(candidates);

        boolean[] cached = new boolean[batch.size()];
        List<Candidate<R>> uncached = new ArrayList<>();
        for (int i = 0; i < batch.size(); i++) {
            if (cache.containsKey(batch.get(i).representation())) {
                cached[i] = true;
            } else {
                uncached.add(batch.get(i));
            }
        }

        List<EvaluationOutcome<R>> forwarded =
                uncached.isEmpty() ? List.of() : inner.evaluate(uncached);
        if (forwarded.size() != uncached.size()) {
            throw new IllegalStateException("inner evaluator returned " + forwarded.size()
                    + " outcomes for " + uncached.size() + " candidates");
        }
        for (EvaluationOutcome<R> outcome : forwarded) {
            if (outcome instanceof EvaluationOutcome.Success<R> success) {
                cache.put(success.candidate().representation(), success.result().evaluation());
            }
        }

        List<EvaluationOutcome<R>> outcomes = new ArrayList<>(batch.size());
        int nextForwarded = 0;
        for (int i = 0; i < batch.size(); i++) {
            if (cached[i]) {
                Candidate<R> candidate = batch.get(i);
                cacheHitCount++;
                outcomes.add(new EvaluationOutcome.Success<>(new EvaluatedCandidate<>(
                        candidate, cache.get(candidate.representation()))));
            } else {
                outcomes.add(forwarded.get(nextForwarded++));
            }
        }
        return Collections.unmodifiableList(outcomes);
    }

    @Override
    public long evaluationCount() {
        return inner.evaluationCount();
    }

    /**
     * Returns the cumulative number of requests served from the cache.
     *
     * @return the hit count, {@code >= 0}
     */
    public long cacheHitCount() {
        return cacheHitCount;
    }
}
