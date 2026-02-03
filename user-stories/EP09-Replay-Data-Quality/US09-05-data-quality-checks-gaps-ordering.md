# User Story US09-05 — Data quality checks for gaps and ordering

## Description
As a Data Quality Engineer, I want automated checks for gaps and ordering issues so that data quality regressions are detected.

## Components
- Backend: Batch or streaming jobs analyzing event streams.
- Infra: Metrics and reports for data quality.

## Acceptance Criteria
- Checks detect missing sequences, out-of-order events, and stale data.
- Data quality metrics are exposed per venue and instrument.
- Failing checks can raise alerts or create tickets.
