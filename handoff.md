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

### S3 — Deterministic random infrastructure — `committed (this commit)`

Scope: random-source abstraction; one root seed; independent named child streams; stable name→seed derivation (never `String.hashCode()`, never shared mutable RNG across components or threads).

Open decisions:
1. Underlying generator algorithm (JDK 21 `RandomGenerator` family — which one).
2. Name→child-seed derivation method.
3. API surface (which `next*` methods, bounded variants).

Done when: reproducibility tests pass — same root seed ⇒ identical streams; distinct names ⇒ independent streams.

Built: `com.axiometa.random` with `RandomSource` (minimal owned contract: `nextInt(bound)`, `nextDouble()`, `nextBoolean()`, `child(name)`; documented single-owner/no-thread-sharing rule) and `SeededRandomSource` (`root(seed)` factory; JDK `L64X128MixRandom` per D32; SHA-256 name→seed derivation per D33, independent of draw history). 14 tests cover stream identity for equal seeds, independence across seeds/names/parents/paths, re-derivation identity, derivation's order-insensitivity, range and validation behavior, and a golden-value change detector pinning the generator and derivation outputs.

### S4 — Sequential evaluator — `todo`

Scope: evaluator abstraction (batch evaluation of candidates against a `Problem<R>`, input order preserved regardless of completion order) plus the sequential implementation.

Open decisions:
1. Interface signature (batch and/or single-candidate).
2. Evaluation-count accounting (where tracked, how exposed).
3. Failure semantics (propagate immediately vs collect).

Done when: order-preservation, counting, and failure tests pass.

### S5 — Caching evaluator decorator — `todo`

Scope: in-memory fitness cache wrapping any evaluator. Caches evaluations, not candidate identity. Must never silently convert a stochastic problem into a deterministic one.

Open decisions:
1. How a `Problem` declares deterministic vs stochastic evaluation.
2. Cache key definition (ties to the S1 fingerprint decision).
3. Cache scope, limits, eviction (unbounded first or bounded).
4. Accounting: cache hits vs real evaluations.

Done when: hit/miss tests, stochastic-bypass tests, and accounting tests pass.

### S6 — Algorithm lifecycle & termination — `todo`

Scope: minimal algorithm lifecycle contract (thin orchestrator per D13); concrete composable termination conditions implementing the S2 termination contract (e.g. max evaluations, max iterations, and/or combinators).

Open decisions:
1. Lifecycle shape (step-based vs run-to-completion).
2. Initial combinator set.

Done when: stub-algorithm tests exercise the lifecycle and termination composition.

### S7 — Genetic Algorithm end-to-end — `todo`

Scope: one simple GA (D10) as a thin orchestrator (D13) over concrete implementations of the S2 phase contracts, built only from S1–S6 pieces; one toy problem as test fixture; deterministic end-to-end reproducibility test. Implement from an approved specification with citation (`CLAUDE.md` → Algorithm Provenance) — no code copied from other optimization libraries.

Open decisions:
1. GA flavor for this slice (generational or not, elitism, selection operator).
2. Toy problem and representation (e.g. OneMax on a bitstring vs sphere on a real vector).
3. Concrete phase components required (initializer, selection, crossover, mutation, replacement) and their parameters.
4. GA configuration API shape.
5. Where the toy problem lives (test sources vs example package).

Done when: fixed seed ⇒ identical results across runs; GA reaches the toy optimum in tests; full suite green.

## After S7

Everything else in `CLAUDE.md`'s long-term list — NSGA-II, SPEA2, AMOSA, simulated annealing, multi-objective infrastructure, thread-pool and remote evaluators, island models, cellular EAs, replacement policies, experiment export, Python analysis tooling — is **not authorized**. Re-plan with the user after the S7 review.
