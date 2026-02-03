# User Story US02-05 — Deterministic ordering in normalization stage

## Description
As an Algo Developer, I want canonical events to preserve a deterministic per-instrument ordering so that I can reason about event sequences.

## Components
- Backend: Partition-aware processing in the normalizer.
- Infra: Configuration for partitioning by instrument or instrument group.

## Acceptance Criteria
- Normalizer processes events in a single-writer fashion per partition.
- For a given instrument and partition, canonical events are emitted in a deterministic order consistent with sequence numbers and timestamps.
- Ordering guarantees are documented for consumers.
