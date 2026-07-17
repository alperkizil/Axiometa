package com.axiometa.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axiometa.bitstring.BitString;
import com.axiometa.core.Evaluation;
import com.axiometa.core.EvaluationSemantics;
import com.axiometa.core.Objective;
import com.axiometa.core.ObjectiveSense;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OneMaxProblemTest {

    private final OneMaxProblem problem = new OneMaxProblem();

    private static BitString bits(String pattern) {
        List<Boolean> bits = new ArrayList<>(pattern.length());
        for (char bit : pattern.toCharArray()) {
            bits.add(bit == '1');
        }
        return new BitString(bits);
    }

    @Test
    void countsTheOneBits() {
        Evaluation evaluation = problem.evaluate(bits("10110"));

        assertEquals(3.0, evaluation.objectiveValue(0));
        assertEquals(0, evaluation.constraintViolationCount());
    }

    @Test
    void scoresTheBoundaryStrings() {
        assertEquals(0.0, problem.evaluate(bits("0000")).objectiveValue(0));
        assertEquals(4.0, problem.evaluate(bits("1111")).objectiveValue(0));
    }

    @Test
    void declaresOneMaximizedObjectiveNoConstraintsDeterministic() {
        assertEquals(List.of(new Objective("ones", ObjectiveSense.MAXIMIZE)),
                problem.objectives());
        assertEquals(List.of(), problem.constraints());
        assertEquals(EvaluationSemantics.DETERMINISTIC, problem.evaluationSemantics());
    }

    @Test
    void rejectsANullBitString() {
        assertThrows(NullPointerException.class, () -> problem.evaluate(null));
    }
}
