# User Story US03-06 — Golden tests for deterministic book reconstruction

## Description
As a QA Engineer, I want golden tests that reconstruct books from recorded deltas so that book logic regressions are detected quickly.

## Components
- Backend: Test harness for replaying sequences of BOOK_SNAPSHOT and BOOK_DELTA events.
- Docs: Process for generating and updating book golden fixtures.

## Acceptance Criteria
- For selected instruments, applying recorded deltas reproduces the expected final book state.
- Tests cover gap scenarios and recoverable inconsistencies.
- Tests run in CI and fail on reconstruction mismatches.
