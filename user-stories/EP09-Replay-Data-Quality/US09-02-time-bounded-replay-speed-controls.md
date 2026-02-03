# User Story US09-02 — Time-bounded replay service with speed controls

## Description
As a Market Data Engineer, I want a time-bounded replay service so that I can reproduce scenarios for testing and analysis.

## Components
- Backend: Replay service that reads from the persistent store.
- Infra: Configuration for time ranges and speed modes.

## Acceptance Criteria
- Replay service supports specifying instrument(s) and time range.
- Replay can run in real-time or accelerated modes.
- Replay jobs expose progress and status metrics.
