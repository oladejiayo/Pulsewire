# Epic EP02 — Canonical Schema & Normalization

Scope: FR2, sections on canonical data model and schema evolution.

Goal: Define and implement a canonical market event schema and normalization pipeline that converts raw vendor messages into validated, ordered, versioned canonical events.

User stories:

- US02-01 — Define canonical market event schemas
- US02-02 — Implement schema registry service (basic)
- US02-03 — Implement normalizer service for TRADE and QUOTE
- US02-04 — Validation and error handling for canonical events
- US02-05 — Deterministic ordering in normalization stage
- US02-06 — Schema versioning and compatibility checks
- US02-07 — Golden fixture tests for normalization
