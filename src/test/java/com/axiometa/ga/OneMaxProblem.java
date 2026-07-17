package com.axiometa.ga;

import com.axiometa.bitstring.BitString;
import com.axiometa.core.Constraint;
import com.axiometa.core.Evaluation;
import com.axiometa.core.EvaluationSemantics;
import com.axiometa.core.Objective;
import com.axiometa.core.ObjectiveSense;
import com.axiometa.core.Problem;
import java.util.List;
import java.util.Objects;

/**
 * OneMax toy problem: maximize the number of one-bits. The optimum is the
 * all-ones string with fitness equal to the bit length. Deterministic,
 * unconstrained — the canonical GA testbed.
 */
final class OneMaxProblem implements Problem<BitString> {

    @Override
    public List<Objective> objectives() {
        return List.of(new Objective("ones", ObjectiveSense.MAXIMIZE));
    }

    @Override
    public List<Constraint> constraints() {
        return List.of();
    }

    @Override
    public Evaluation evaluate(BitString representation) {
        Objects.requireNonNull(representation, "representation must not be null");
        long ones = representation.bits().stream().filter(bit -> bit).count();
        return new Evaluation(new double[] {ones}, new double[0]);
    }

    @Override
    public EvaluationSemantics evaluationSemantics() {
        return EvaluationSemantics.DETERMINISTIC;
    }
}
