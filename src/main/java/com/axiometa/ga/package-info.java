/**
 * Generational genetic algorithm and its generic phase components.
 *
 * <p>Specification provenance: the generational GA cycle with k-tournament
 * parent selection and elitist full-generation replacement as specified in
 * A.E. Eiben and J.E. Smith, <em>Introduction to Evolutionary Computing</em>,
 * 2nd ed., Springer, 2015 (ch. 3 the evolutionary cycle; ch. 5 tournament
 * selection and generational population management), and D.E. Goldberg,
 * <em>Genetic Algorithms in Search, Optimization and Machine Learning</em>,
 * Addison-Wesley, 1989. Elitism follows K.A. De Jong, <em>An Analysis of the
 * Behavior of a Class of Genetic Adaptive Systems</em>, PhD thesis,
 * University of Michigan, 1975. No code was taken from any optimization
 * library.
 *
 * <p>The components in this package are single-objective and unconstrained:
 * fitness comparisons defensively reject evaluations that carry more than one
 * objective value or any constraint violations rather than silently
 * mis-ranking them.
 */
package com.axiometa.ga;
