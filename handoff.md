# Axiometa — Implementation Handoff

Read `CLAUDE.md` first. It is the binding rulebook for process, communication, scope discipline, and quality. This document adds the approved decisions and the slice roadmap. If the two conflict, `CLAUDE.md` wins.

## Mission

Axiometa is a reusable, domain-independent Java research framework for single- and multi-objective metaheuristics (full long-term list: `CLAUDE.md` → Objective). Current phase: establish the basics as eight small vertical slices, S0–S7, ending with one Genetic Algorithm running end-to-end.

## How to use this document

1. Read `CLAUDE.md`, then this file.
2. Find the first slice in the roadmap whose status is not `reviewed`.
3. Confirm the previous slice's Codex review is complete and the user has explicitly said to continue.
4. Ask the user that slice's **Open decisions** as concise numbered questions with options and consequences. Wait for explicit answers.
5. State the exact bounded scope. Wait for approval if it contains anything not already approved.
6. Implement only that scope. Verify. Self-review. Report. Ask permission to commit. Commit. Stop.
7. When a decision is approved, add it to the decision registry and update the slice status in the same commit as the work it governs.

Never implement ahead of the current slice. Never resolve an open decision yourself. Never present an unapproved recommendation as a decision.

## Decision registry

Approved by the user. Do not re-ask. Do not reopen without a concrete technical reason.

| #   | Decision                 | Value                                                                          |
|-----|--------------------------|--------------------------------------------------------------------------------|
| D1  | Project / framework name | Axiometa                                                                       |
| D2  | Maven coordinates        | groupId `com.axiometa`, artifactId `axiometa`                                  |
| D3  | Java package root        | `com.axiometa`                                                                 |
| D4  | Java version             | JDK 21 (compile with `--release 21`)                                           |
| D5  | Build system             | Maven                                                                          |
| D6  | Module layout            | Single module now; grow incrementally; split only on concrete need             |
| D7  | License                  | GPL-3.0 (existing `LICENSE` kept)                                              |
| D8  | Test stack               | JUnit 5 only; small focused unit tests                                         |
| D9  | Build strictness         | `-Xlint:all` + `-Werror` from S0; static-analysis tools deferred               |
| D10 | First end-to-end algorithm | Genetic Algorithm (S7)                                                       |
| D11 | Decision registry location | This file (`handoff.md`)                                                     |
| D12 | S0 extras                | README update only — Maven wrapper and `.gitignore` explicitly declined        |
| D13 | Phase-component principle | Algorithm phases (initialization, selection, crossover, mutation, replacement, termination) are pluggable, independently testable components; algorithm loops are thin orchestrators |
| D14 | Phase-contract placement | All six phase contracts declared up front in S2, including termination; S6 implements the lifecycle and concrete termination combinators against the S2 contract |
| D15 | Phase common ancestor    | Marker super-interface `AlgorithmPhase` (no members) extended by all six phase contracts; not an abstract class |
| D16 | Initial project version  | `0.1.0-SNAPSHOT`                                                               |
| D17 | S0 pinned build versions | maven-compiler-plugin 3.15.0; maven-surefire-plugin 3.5.6; junit-jupiter 5.12.2 |
| D18 | S1 core type names       | `Candidate<R>` representation wrapper; `Evaluation` result type                |
| D19 | Objective sense & values | `ObjectiveSense` enum (`MINIMIZE`/`MAXIMIZE`) per `Objective`; `Evaluation` stores doubles in the problem's declared objective order with indexed access; no internal normalization |
| D20 | Constraint convention    | Violation magnitude `double >= 0`; `0.0` = satisfied; feasible iff all violations zero; equality tolerance is the constraint author's responsibility (no framework epsilon) |
| D21 | Candidate equality       | Representations must define value-based `equals`/`hashCode`; `Candidate` delegates to its representation; contractual basis for the S5 cache key |
| D22 | Core exception convention | JDK unchecked only: `NullPointerException` for nulls, `IllegalArgumentException` for invalid values, `IndexOutOfBoundsException` for accessor indices; messages name the parameter and rule |
| D23 | Deep immutability        | Core model types are deeply immutable: final types and fields, defensive copies on input, no mutable internals exposed |
| D24 | Core package             | S1 core model lives in `com.axiometa.core`                                     |
| D25 | Build-output ignore      | `.gitignore` ignoring `target/` (approved in S1, revising the S0-era decline recorded in D12) |
| D26 | Representation contract  | Marker `interface Representation` documenting the D21/D23 rules; the six phase contracts bound `<R extends Representation>`; S1 types and data-carrying records stay unbounded |
| D27 | Operator arities         | Crossover 2→2 via dedicated `OffspringPair<R>` record; mutation 1→1; no n-ary support |
| D28 | Component randomness     | Constructor injection of a per-component named random stream (S3 type); phase signatures carry no randomness parameter |
| D29 | Population abstraction   | Immutable non-empty `record Population<R>` over `EvaluatedCandidate<R>` (candidate + evaluation pairing); selection consumes populations, replacement produces them |
| D30 | Termination observation  | Immutable `record AlgorithmState<R>` (iteration, evaluationCount, population) observed by `Termination.shouldTerminate(state)`; termination implementations may keep internal state across calls |
| D31 | S2 packages              | Model additions (`Representation`, `EvaluatedCandidate`, `Population`) in `com.axiometa.core`; `AlgorithmPhase`, the six phase contracts, `OffspringPair`, `AlgorithmState` in `com.axiometa.phase` |
| D32 | Random generator         | JDK `L64X128MixRandom`, one instance per stream, created via `RandomGeneratorFactory` from a 64-bit seed |
| D33 | Child-seed derivation    | child seed = first 8 bytes (big-endian) of SHA-256(parent seed as 8 big-endian bytes \|\| stream name as UTF-8); pure function of (seed, name) — same name re-derives the identical stream, so wiring must give distinct components distinct names |
| D34 | Random API surface       | `interface RandomSource { nextInt(bound), nextDouble(), nextBoolean(), child(name) }` implemented by `final SeededRandomSource` with static `root(seed)`; instances stateful, not thread-safe, one owner each; golden-value test pins generator + derivation |
| D35 | Random package           | Random infrastructure lives in `com.axiometa.random`                           |
| D36 | Evaluator signature      | `interface Evaluator<R>` bound to its `Problem<R>` at construction; batch-only `evaluate(List<Candidate<R>>)` returning `List<EvaluationOutcome<R>>` in input order (amended from plain results by D38); single candidate = batch of one |
| D37 | Evaluation accounting    | `long evaluationCount()` on `Evaluator` = cumulative `Problem.evaluate` attempts (counted whether or not the attempt completes); decorators avoiding real work must surface the wrapped evaluator's count |
| D38 | Failure semantics        | Collect failures as outcomes: sealed `EvaluationOutcome<R>` with `Success(EvaluatedCandidate)` / `Failure(candidate, RuntimeException)`; batch continues after failures; JVM `Error`s propagate immediately; caller input bugs (null list/elements) are thrown, never collected; problem-contract violations (null/mismatched evaluation) become failures carrying `IllegalStateException` |
| D39 | Evaluation package       | Evaluation infrastructure lives in `com.axiometa.evaluation`                   |
| D40 | Evaluation semantics     | `enum EvaluationSemantics { DETERMINISTIC, STOCHASTIC }` in core; `Problem` gains required `evaluationSemantics()` (S1 amendment approved here); no default — every problem declares explicitly |
| D41 | Cache behavior           | `CachingEvaluator<R>` decorates any evaluator: key = representation value (D21), per-problem scope via D36; unbounded, no eviction; only successes cached — failures retry; within one batch, duplicates of an uncached representation are all forwarded (batching preserved); stochastic problems bypass the cache entirely |
| D42 | Cache accounting         | `evaluationCount()` delegates to the wrapped evaluator (stack top = real attempts, D37); `cacheHitCount()` = requests served from cache |
| D43 | Lifecycle shape          | Step-based: `Algorithm<R>` exposes `initialize()` and `step(state)` over immutable `AlgorithmState`; the single shared loop lives in `AlgorithmRunner.run(algorithm, termination)`; termination is passed to the runner, never owned by the algorithm, and observes every state including generation 0 |
| D44 | Termination set          | `Terminations` factories: `maxIterations(int)`, `maxEvaluations(long)` (limits >= 1), `anyOf(...)`, `allOf(...)`; combinators consult every member on every check (no short-circuit) so stateful conditions observe the full state sequence |
| D45 | Algorithm package        | Lifecycle and terminations live in `com.axiometa.algorithm`; `com.axiometa.phase` stays contracts-only |
| D46 | GA flavor & provenance   | Generational GA with configurable elitism and k-tournament selection; spec: Eiben & Smith 2015 ch. 3–5, Goldberg 1989, elitism De Jong 1975; single-objective and unconstrained only — fitness comparisons defensively reject anything else |
| D47 | Toy problem              | OneMax on immutable `BitString` (record over `List<Boolean>`); operators: one-point crossover, per-bit flip mutation; the OneMax fitness fixture lives in test sources |
| D48 | S7 components            | `RandomBitStringInitialization(bitCount, random)`; `OnePointCrossover(probability, random)` — clones parents when the probability draw fails or length < 2; `BitFlipMutation(perBitProbability, random)` — one draw per bit; `TournamentSelection(sense, k, random)` — with-replacement sampling, ties keep the first sampled; `GenerationalReplacement(sense, elitismCount)` — best-e of current (stable sort) + offspring in order, sized to the current population |
| D49 | GA config & packages     | Plain 7-argument constructor; even `populationSize >= 2`; per-step flow: select N parents → pair in order → crossover → mutate both children → evaluate → replace; any evaluation failure → `IllegalStateException`; packages `com.axiometa.ga` (generic GA pieces) and `com.axiometa.bitstring` (representation + operators) |
| D50 | Runnable example package | User-requested demo: `com.axiometa.example` in main sources with a public `OneMaxProblem` and `OneMaxExample` (a `main` wiring the full stack: named streams → cached sequential evaluation → GA phase components → composed termination → shared runner); mirrors, not replaces, the S7 test fixture (D47); example code is not framework API and has its own unit tests |

## Working protocol (condensed)

Per slice:

1. Inspect the repository read-only; read the registry and the slice entry.
2. Ask the slice's open decisions. Wait for explicit answers.
3. State the exact bounded scope. Wait for approval of anything new.
4. Implement only that scope.
5. Run `mvn clean verify` — must pass with zero warnings.
6. Self-review the full diff: correctness, duplication, unnecessary complexity, thread safety, accidental API expansion.
7. Report the diff summary and verification results. Ask permission to commit.
8. After approval: one atomic commit with a precise message. Report the hash. Stop for Codex review.

Never amend, squash, rebase, or force-push anything that may be under Codex review. When Codex reports an issue, fix only that issue and its directly necessary tests.

## Slice roadmap

Statuses: `todo` → `in progress` → `committed <hash>` → `reviewed`.

### S0 — Project skeleton — `reviewed`

Scope: single-module `pom.xml` (D2, D4, D5, D9; JUnit 5 test-scope dependency; compiler and surefire plugins pinned), `src/main/java/com/axiometa/`, `src/test/java/com/axiometa/`, one placeholder test proving build + test execution, README replaced with a short Axiometa description (D12).

Open decisions:
1. Initial project version (e.g. `0.1.0-SNAPSHOT`).
2. Exact pinned versions for compiler plugin, surefire, and JUnit — implementer proposes current stable versions in the scope statement.

Done when: `mvn clean verify` passes with zero warnings and runs the placeholder test.

### S1 — Immutable core model — `reviewed`

Scope: typed `Problem<R>`, candidate/solution wrapper, objective declarations with min/max sense, constraint declarations, evaluation-result type. Deep immutability, defensive validation, documented contracts. No algorithm code.

Open decisions:
1. Public type names (e.g. `Candidate` vs `Solution`; `Evaluation` vs `Fitness`).
2. Objective-sense representation and how objective values are stored and accessed.
3. Constraint convention (violation magnitude vs boolean feasibility; equality tolerance).
4. Candidate equality / hashing / fingerprint rules (caching in S5 depends on this — decide the contract now or explicitly defer).
5. Exception types and messages for invalid construction.
6. Formal approval of the deep-immutability rule for core types.

Done when: contracts documented; unit tests cover normal, edge, and failure cases.

Built: package `com.axiometa.core` with six public types — `Problem<R>` (declarations + `evaluate(R)`, documented contract, thread-safety deferred to S4), `Candidate<R>` (record; equality delegates to the representation per D21), `Objective` (name + `ObjectiveSense`), `ObjectiveSense` (`MINIMIZE`/`MAXIMIZE`), `Constraint` (name only; convention per D20), `Evaluation` (final class; defensive copies, indexed accessors, `isFeasible()`, exact-representation value equality) — plus `package-info` documenting the D23 immutability rule and D22 exception conventions. 32 unit tests across five classes cover construction failures with exact messages, defensive copying, index bounds, feasibility (including `-0.0`), equality semantics, and a minimal test-only `Problem<Double>` fixture proving the contracts compose. `BuildSanityTest` removed as superseded.

### S2 — Representation & phase-component contracts — `reviewed`

Scope: representation-type contract; marker super-interface `AlgorithmPhase` (D15); typed contracts for all six algorithm phases — initialization, selection, crossover, mutation, replacement, termination (D13, D14) — each extending `AlgorithmPhase`. Contracts only — no concrete implementations.

Open decisions:
1. Contract names and exact signatures.
2. Operator arities (crossover 2→2 vs 2→1; n-ary support or not).
3. How components receive randomness (constructor injection vs per-call random-source parameter).
4. What population/collection abstraction selection and replacement operate on (may require a population type on top of S1).
5. What state the termination contract observes (decided here per D14, ahead of the S6 lifecycle design).

Done when: contracts compile against S1 types, are documented, and have minimal type-level tests using test doubles.

Built: `com.axiometa.core` gained `Representation` (marker carrying the D21/D23 rules), `EvaluatedCandidate<R>` (candidate + evaluation pairing), and `Population<R>` (immutable, non-empty, defensively copied). New package `com.axiometa.phase` holds the `AlgorithmPhase` marker (D15) and the six bounded phase contracts — `Initialization` (`initialize(populationSize)`), `Selection` (`select(population, count)`), `Crossover` (`crossover(first, second)` returning `OffspringPair<R>`), `Mutation` (`mutate(candidate)`), `Replacement` (`replace(current, offspring)`), `Termination` (`shouldTerminate(AlgorithmState<R>)`) — plus the `OffspringPair<R>` and `AlgorithmState<R>` records and a package doc recording the D13/D28 rules. Contracts only; no concrete phases, no randomness types, no S1 modifications. Tests: record validation/defensive-copy/equality suites plus `PhaseContractsTest`, which composes deterministic test doubles of all six contracts into one full iteration over the core types and asserts the shared `AlgorithmPhase` ancestor.

### S3 — Deterministic random infrastructure — `reviewed`

Scope: random-source abstraction; one root seed; independent named child streams; stable name→seed derivation (never `String.hashCode()`, never shared mutable RNG across components or threads).

Open decisions:
1. Underlying generator algorithm (JDK 21 `RandomGenerator` family — which one).
2. Name→child-seed derivation method.
3. API surface (which `next*` methods, bounded variants).

Done when: reproducibility tests pass — same root seed ⇒ identical streams; distinct names ⇒ independent streams.

Built: `com.axiometa.random` with `RandomSource` (minimal owned contract: `nextInt(bound)`, `nextDouble()`, `nextBoolean()`, `child(name)`; documented single-owner/no-thread-sharing rule) and `SeededRandomSource` (`root(seed)` factory; JDK `L64X128MixRandom` per D32; SHA-256 name→seed derivation per D33, independent of draw history). 14 tests cover stream identity for equal seeds, independence across seeds/names/parents/paths, re-derivation identity, derivation's order-insensitivity, range and validation behavior, and a golden-value change detector pinning the generator and derivation outputs.

### S4 — Sequential evaluator — `reviewed`

Scope: evaluator abstraction (batch evaluation of candidates against a `Problem<R>`, input order preserved regardless of completion order) plus the sequential implementation.

Open decisions:
1. Interface signature (batch and/or single-candidate).
2. Evaluation-count accounting (where tracked, how exposed).
3. Failure semantics (propagate immediately vs collect).

Done when: order-preservation, counting, and failure tests pass.

Built: `com.axiometa.evaluation` with the sealed `EvaluationOutcome<R>` (`Success`/`Failure`, common `candidate()` accessor, exhaustive pattern matching), the `Evaluator<R>` contract (problem bound at construction per D36; one outcome per candidate in input order; attempt counting per D37; collect-failures semantics per D38), and `SequentialEvaluator<R>` (in-order on the calling thread; defensively verifies the problem's evaluation counts and null contract, converting violations to `IllegalStateException` failures; immutable result lists; single-owner counter). 18 tests cover ordering/pairing, cumulative and failed-attempt counting, mixed and all-failure batches, `Error` propagation, contract-violation detection, input validation before any evaluation, result immutability, and outcome record validation.

### S5 — Caching evaluator decorator — `reviewed`

Scope: in-memory fitness cache wrapping any evaluator. Caches evaluations, not candidate identity. Must never silently convert a stochastic problem into a deterministic one.

Open decisions:
1. How a `Problem` declares deterministic vs stochastic evaluation.
2. Cache key definition (ties to the S1 fingerprint decision).
3. Cache scope, limits, eviction (unbounded first or bounded).
4. Accounting: cache hits vs real evaluations.

Done when: hit/miss tests, stochastic-bypass tests, and accounting tests pass.

Built: `EvaluationSemantics` enum in core and the required `Problem.evaluationSemantics()` declaration (D40; both test fixtures updated). `CachingEvaluator<R>` in `com.axiometa.evaluation`: unbounded representation-value cache (D41) serving hits as successes paired with the requesting candidate, forwarding all uncached requests (within-batch duplicates included) to the wrapped evaluator as one order-preserving batch, caching only successes, bypassing entirely for stochastic problems, delegating `evaluationCount()` and exposing `cacheHitCount()` (D42), and defensively verifying the inner evaluator's outcome count. 14 tests cover hit/miss behavior, ordering and pairing, duplicate forwarding, failure retry, stochastic bypass, accounting arithmetic (hits + real attempts = requests), validation, misbehaving-inner detection, and immutability.

### S6 — Algorithm lifecycle & termination — `reviewed`

Scope: minimal algorithm lifecycle contract (thin orchestrator per D13); concrete composable termination conditions implementing the S2 termination contract (e.g. max evaluations, max iterations, and/or combinators).

Open decisions:
1. Lifecycle shape (step-based vs run-to-completion).
2. Initial combinator set.

Done when: stub-algorithm tests exercise the lifecycle and termination composition.

Built: `com.axiometa.algorithm` with `Algorithm<R>` (step-based lifecycle per D43; thin-orchestrator contract documented), `AlgorithmRunner` (the single shared loop: initialize → check → (step → check)*; defends against null states with naming messages), and `Terminations` (`maxIterations`, `maxEvaluations`, `anyOf`, `allOf` per D44; validated limits and member lists; combinators consult every member on every check). 17 tests: boundary behavior of both budgets, combinator truth tables, the no-short-circuit guarantee via counting spies, full runner runs against a counting stub algorithm with exact stop iterations under single and composed conditions, generation-0 termination, and all validation paths.

### S7 — Genetic Algorithm end-to-end — `committed (this commit)`

Scope: one simple GA (D10) as a thin orchestrator (D13) over concrete implementations of the S2 phase contracts, built only from S1–S6 pieces; one toy problem as test fixture; deterministic end-to-end reproducibility test. Implement from an approved specification with citation (`CLAUDE.md` → Algorithm Provenance) — no code copied from other optimization libraries.

Open decisions:
1. GA flavor for this slice (generational or not, elitism, selection operator).
2. Toy problem and representation (e.g. OneMax on a bitstring vs sphere on a real vector).
3. Concrete phase components required (initializer, selection, crossover, mutation, replacement) and their parameters.
4. GA configuration API shape.
5. Where the toy problem lives (test sources vs example package).

Done when: fixed seed ⇒ identical results across runs; GA reaches the toy optimum in tests; full suite green.

Built: `com.axiometa.bitstring` — `BitString` (immutable record representation), `RandomBitStringInitialization`, `OnePointCrossover` (probability-gated, clone fallback), `BitFlipMutation` (one draw per bit); `com.axiometa.ga` — `TournamentSelection` and `GenerationalReplacement` (generic, sharing the package-private `SingleObjectiveFitness` comparator that defensively rejects multi-objective or constrained evaluations) and `GeneticAlgorithm` (thin orchestrator per D13: select → pair → crossover → mutate → evaluate → replace; defensive checks on component contract violations; evaluation failures raise `IllegalStateException`). Package docs carry the D46 citations. Test sources add the `OneMaxProblem` fixture, scripted random doubles proving exact draw consumption, unit suites for every component, and `OneMaxEndToEndTest`: full-stack wiring (root seed → named streams, sequential evaluator behind the fitness cache, composed termination, shared runner) reaching the 20-bit optimum, bit-identical repeat runs from one seed, seed independence, and elite-monotone best fitness.

## After S7

Everything else in `CLAUDE.md`'s long-term list — NSGA-II, SPEA2, AMOSA, simulated annealing, multi-objective infrastructure, thread-pool and remote evaluators, island models, cellular EAs, replacement policies, experiment export, Python analysis tooling — is **not authorized**. Re-plan with the user after the S7 review.
