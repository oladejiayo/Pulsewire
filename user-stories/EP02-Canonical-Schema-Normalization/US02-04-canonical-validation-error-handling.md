# User Story US02-04 — Validation and error handling for canonical events

## Description
As a Platform Engineer, I want strict validation of canonical events so that malformed data does not propagate to consumers.

## Components
- Backend: Validation layer in the normalizer pipeline.
- Infra: Metrics and logs for validation failures.

## Acceptance Criteria
- Canonical events are validated against the schema registry before publish.
- Validation failures include descriptive error codes and offending field names.
- Validation error rates are exposed via metrics and can be alerted on.
