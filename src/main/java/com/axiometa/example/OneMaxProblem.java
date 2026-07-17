package com.axiometa.example;

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
 * OneMax: maximize the number of one-bits in a fixed-length bit string. The
 * optimum is the all-ones string with fitness equal to the bit length.
 *
 * <p>The canonical first GA problem: one {@code MAXIMIZE} objective, no
 * constraints, deterministic evaluation (equal bit strings always score
 * equally, so results may safely be cached).
 */
public final class OneMaxProblem implements Problem<BitString> {

    private static final List<Objective> OBJECTIVES =
            List.of(new Objective("ones", ObjectiveSense.MAXIMIZE));

    @Override
    public List<Objective> objectives() {
        return OBJECTIVES;
    }

    @Override
    public List<Constraint> constraints() {
        return List.of();
    }

    @Override
    public Evaluation evaluate(BitString representation) {
        Objects.requireNonNull(representation, "representation must not be null");
        int ones = 0;
        for (boolean bit : representation.bits()) {
            if (bit) {
                ones++;
            }
        }
        return new Evaluation(new double[] {ones}, new double[0]);
    }

    @Override
    public EvaluationSemantics evaluationSemantics() {
        return EvaluationSemantics.DETERMINISTIC;
    }
}
