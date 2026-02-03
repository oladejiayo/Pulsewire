# User Story US02-07 — Golden fixture tests for normalization

## Description
As a QA Engineer, I want golden fixture tests for normalization so that regressions in canonical outputs are detected early.

## Components
- Backend: Test harness that replays fixture files through the normalizer.
- Docs: Process for adding and updating golden fixtures.

## Acceptance Criteria
- Golden fixtures exist for at least one venue and several instruments.
- Test harness compares canonical outputs against stored golden references with configurable tolerances.
- Tests are wired into CI and fail the build on output mismatches.
