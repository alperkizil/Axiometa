# Project Instructions for Claude Code

## Objective

Build a reusable, domain-independent Java research framework for implementing and experimenting with single-objective and multi-objective metaheuristics.

The long-term framework should support:

- Arbitrary, strongly typed solution representations.
- Problem-specific representation creation and fitness evaluation.
- Single-objective and multi-objective optimization through one common architecture.
- NSGA-II, SPEA2, AMOSA, Genetic algorithm- Simulated Annealing.
- Master-slave fitness evaluation through a common evaluator abstraction.
- Sequential and local thread-pool evaluators initially; remote workers later.
- Fitness caching with correct deterministic and stochastic evaluation semantics.
- Island-model coordination capable of running heterogeneous algorithms.
- Cellular evolutionary algorithms with replaceable topology definitions.
- Generational and steady-state GA behavior through reusable replacement policies.
- Native minimization, maximization, and constraint infrastructure.
- Composable termination conditions.
- Deterministic, repeatable randomization from one root seed and independent named child streams.
- Standardized experiment export from Java.
- Separate Python and Matplotlib analysis tools for scatter, bar, and later research plots.
- Object-oriented design with high reuse and minimal duplication.

This is a long-term direction, not permission to implement everything immediately.

## Current Goal: Establish the Basics

Work incrementally. Get one small, correct vertical slice running before adding advanced algorithms, distributed execution, plotting, or experiment infrastructure.

The initial work should eventually establish:

1. An approved project and build structure.
2. The minimum immutable core model for a typed `Problem<R>`, candidates, objectives, constraints, and evaluations.
3. A representation-type contract and typed variation-operator contracts.
4. Deterministic random-source infrastructure.
5. A sequential evaluator.
6. Correct in-memory fitness caching as an evaluator decorator.
7. A minimal algorithm lifecycle and termination-condition contract.
8. One simple single-objective algorithm as an end-to-end proof of the design.

Do not treat this list as authorization to implement all eight items at once. Complete only the next explicitly approved slice.

## Communication Rules

Use minimal words. Do not produce long introductions, repeated summaries, motivational commentary, or explanations of obvious actions.

When reporting work, include only:

- Decisions requiring approval.
- The exact work completed.
- Verification results.
- Problems or risks that require attention.
- The resulting commit hash when a commit has been approved and created.

Do not narrate routine tool usage.

## No-Assumption Rule

Do not make project decisions independently.

If a choice has not been explicitly approved by the user, stop and ask before acting. Do not silently select a conventional or recommended default.

This includes, but is not limited to:

- Project name.
- Maven coordinates and Java package root.
- Java version.
- Maven, Gradle, or another build system.
- Single-module or multi-module layout.
- Directory and package structure.
- External dependencies and plugins.
- Public API names and signatures.
- Mutability and ownership rules.
- Serialization formats.
- Exception behavior.
- Equality, hashing, and fingerprinting rules.
- Constraint conventions and equality tolerances.
- Random-number algorithms and seed-derivation methods.
- Cache keys, cache scope, limits, and eviction behavior.
- Fitness-evaluation accounting.
- Threading, ordering, cancellation, and failure semantics.
- Algorithm variants and interpretations of research papers.
- Test frameworks, quality tools, and coverage requirements.
- Documentation format.
- Licensing choices.
- Any refactor that changes an approved contract.

Ask concise, numbered questions. State the available options and relevant consequences. You may identify a recommendation, but do not apply it until the user explicitly approves it.

If the user's answer is ambiguous, ask again. Do not infer intent.

Once a decision is approved, record it in the project's architecture documentation so it does not need to be asked again. Do not reopen an approved decision without a concrete technical reason.

## Implementation Discipline

- Implement only the approved scope.
- Prefer the smallest design that satisfies approved current requirements.
- Do not add speculative extension points merely because they may be useful later.
- Do not implement future milestones early.
- Keep domain logic out of algorithms.
- Keep algorithms independent from concrete evaluator implementations.
- Keep plotting and file export out of algorithm execution.
- Keep representations deeply immutable once that rule is approved in the core API.
- Do not expose mutable internal arrays or collections.
- Do not share one mutable RNG across components or threads.
- Do not use thread scheduling, completion order, object identity, or `String.hashCode()` as a source of reproducible behavior.
- Cache fitness evaluations, not candidate identity.
- Never let caching silently convert a stochastic problem into a deterministic one.
- Preserve evaluator input order regardless of parallel completion order.
- Avoid unchecked casts, raw generic types, global mutable state, hidden singletons, and reflection-based magic.
- Use clear names and cohesive classes.
- Favor composition over inheritance unless inheritance expresses a genuine substitutable relationship.
- Avoid duplicated algorithm loops when a tested policy or reusable component expresses the variation.
- Treat warnings as defects unless an approved exception is documented.

## Quality Requirements

Produce code suitable for a reusable research library, not a disposable prototype.

Every approved implementation slice must include:

- Focused automated tests for normal behavior, edge cases, and failure behavior.
- Deterministic tests without arbitrary sleeps or timing assumptions.
- Defensive validation at public boundaries.
- Clear exception messages.
- Documentation for public contracts and non-obvious invariants.
- No unrelated formatting or refactoring.
- A full build and test run before requesting commit approval.
- A self-review of the diff for correctness, duplication, unnecessary complexity, thread safety, and accidental API expansion.

An implementation is not complete merely because it compiles.

## Algorithm Provenance and MOEA Framework

Do not copy production source code from MOEA Framework or another optimization library unless the user explicitly authorizes that exact reuse after reviewing its licensing consequences.

Implement algorithms from their original papers or other approved specifications. Record citations and map important implementation steps to the specification. MOEA Framework may be used as an external behavioral reference or test oracle only after explicit approval.

## Commit and Review Protocol

ChatGPT Codex will inspect Claude Code's commits.

Use this process for every implementation slice:

1. Inspect the repository and approved decisions without modifying files.
2. Ask the user about every unresolved decision relevant to the next slice.
3. Wait for explicit answers.
4. State the exact bounded implementation scope.
5. Wait for approval if the scope introduces any decision not already approved.
6. Implement only that scope.
7. Run the relevant tests and the full verification command.
8. Self-review the complete diff.
9. Report the diff summary and verification results.
10. Ask for permission to create the commit.
11. After approval, create one small atomic commit with a precise message.
12. Report the commit hash and stop. Do not begin the next slice until Codex inspection is complete and the user explicitly continues.

Do not combine unrelated work in one commit. Do not amend, squash, rebase, force-push, or rewrite a commit that may already be under Codex review unless explicitly instructed.

When Codex reports an issue, address only the reviewed issue and directly necessary tests. Ask before changing an approved architecture decision.

## First Interaction

Do not create the project immediately.

Begin by inspecting the current repository read-only. Then ask the minimum numbered decision questions required to create only the initial project skeleton. At minimum, obtain explicit decisions for the project name, package/Maven coordinates, Java version, build system, module layout, license, and initial testing/quality tools.
