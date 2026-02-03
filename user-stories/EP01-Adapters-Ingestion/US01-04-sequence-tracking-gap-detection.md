# User Story US01-04 — Implement sequence tracking and gap detection

## Description
As a Platform Engineer, I want ingestion adapters to track per-instrument sequence numbers and detect gaps so that data quality issues can be surfaced quickly.

## Components
- Backend: Sequence tracking logic per instrument/venue in adapters.
- Infra: Metrics export to Prometheus and structured logs for gaps.

## Acceptance Criteria
- Each adapter tracks sequence numbers by instrument or feed partition.
- Gaps and out-of-order messages increment per-venue metrics and emit structured logs.
- Gap detection metrics are queryable per instrument and venue in the monitoring system.
