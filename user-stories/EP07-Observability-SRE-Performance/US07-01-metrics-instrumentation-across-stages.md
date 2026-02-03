# User Story US07-01 — Metrics instrumentation across stages

## Description
As an SRE, I want consistent metrics across ingestion, normalization, book building, backbone, and gateways so that I can measure end-to-end performance.

## Components
- Backend: Micrometer metrics instrumentation in all services.
- Infra: Prometheus scraping and retention configuration.

## Acceptance Criteria
- Metrics include latency per stage, queue depths, consumer lag, and drop counts.
- All metrics are tagged with tenant, environment, and partition where applicable.
- Metrics are documented and discoverable in the monitoring system.
