# User Story US07-07 — Performance and soak test harness

## Description
As a Performance Engineer, I want a performance and soak test harness so that I can validate latency and throughput under sustained load.

## Components
- Backend: Load generator and test scenarios.
- Infra: CI integration and environment configuration for soak tests.

## Acceptance Criteria
- Harness can generate configurable message rates across multiple instruments.
- Tests measure end-to-end latency and throughput and compare against budgets.
- Regression gates in CI fail builds when performance falls outside thresholds.
