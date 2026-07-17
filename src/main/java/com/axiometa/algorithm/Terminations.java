package com.axiometa.algorithm;

import com.axiometa.core.Representation;
import com.axiometa.phase.AlgorithmState;
import com.axiometa.phase.Termination;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Factories for concrete, composable termination conditions.
 *
 * <p>The combinators consult <em>every</em> member condition on every check —
 * there is no short-circuiting — so stateful conditions always observe the
 * complete sequence of states, then the individual decisions are combined.
 */
public final class Terminations {

    private Terminations() {
    }

    /**
     * Stops once the completed iteration count reaches {@code limit}.
     *
     * @param <R>   the representation type
     * @param limit iteration budget; must be {@code >= 1}
     * @return a condition that stops when {@code iteration() >= limit}
     * @throws IllegalArgumentException if {@code limit < 1}
     */
    public static <R extends Representation> Termination<R> maxIterations(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be >= 1 but was " + limit);
        }
        return state -> requireState(state).iteration() >= limit;
    }

    /**
     * Stops once the cumulative evaluation count reaches {@code limit}.
     *
     * @param <R>   the representation type
     * @param limit evaluation budget; must be {@code >= 1}
     * @return a condition that stops when {@code evaluationCount() >= limit}
     * @throws IllegalArgumentException if {@code limit < 1}
     */
    public static <R extends Representation> Termination<R> maxEvaluations(long limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be >= 1 but was " + limit);
        }
        return state -> requireState(state).evaluationCount() >= limit;
    }

    /**
     * Stops when any member condition stops.
     *
     * @param <R>        the representation type
     * @param conditions member conditions; at least one, none null
     * @return the disjunction of the members
     * @throws NullPointerException     if the array or an element is null
     * @throws IllegalArgumentException if no condition is given
     */
    @SafeVarargs
    public static <R extends Representation> Termination<R> anyOf(
            Termination<R>... conditions) {
        // validation stays inline: letting the varargs array escape to a
        // helper trips -Xlint:varargs despite @SafeVarargs
        if (conditions == null) {
            throw new NullPointerException("conditions must not be null");
        }
        if (conditions.length == 0) {
            throw new IllegalArgumentException("conditions must not be empty");
        }
        List<Termination<R>> members = new ArrayList<>(conditions.length);
        for (int i = 0; i < conditions.length; i++) {
            if (conditions[i] == null) {
                throw new NullPointerException("conditions[" + i + "] must not be null");
            }
            members.add(conditions[i]);
        }
        return combine(List.copyOf(members), true);
    }

    /**
     * Stops when all member conditions stop.
     *
     * @param <R>        the representation type
     * @param conditions member conditions; at least one, none null
     * @return the conjunction of the members
     * @throws NullPointerException     if the array or an element is null
     * @throws IllegalArgumentException if no condition is given
     */
    @SafeVarargs
    public static <R extends Representation> Termination<R> allOf(
            Termination<R>... conditions) {
        // validation stays inline: letting the varargs array escape to a
        // helper trips -Xlint:varargs despite @SafeVarargs
        if (conditions == null) {
            throw new NullPointerException("conditions must not be null");
        }
        if (conditions.length == 0) {
            throw new IllegalArgumentException("conditions must not be empty");
        }
        List<Termination<R>> members = new ArrayList<>(conditions.length);
        for (int i = 0; i < conditions.length; i++) {
            if (conditions[i] == null) {
                throw new NullPointerException("conditions[" + i + "] must not be null");
            }
            members.add(conditions[i]);
        }
        return combine(List.copyOf(members), false);
    }

    private static <R extends Representation> Termination<R> combine(
            List<Termination<R>> members, boolean stopWhenAny) {
        return state -> {
            requireState(state);
            boolean anyStopped = false;
            boolean allStopped = true;
            for (Termination<R> member : members) {
                boolean stop = member.shouldTerminate(state);
                anyStopped |= stop;
                allStopped &= stop;
            }
            return stopWhenAny ? anyStopped : allStopped;
        };
    }

    private static <R extends Representation> AlgorithmState<R> requireState(
            AlgorithmState<R> state) {
        return Objects.requireNonNull(state, "state must not be null");
    }
}
