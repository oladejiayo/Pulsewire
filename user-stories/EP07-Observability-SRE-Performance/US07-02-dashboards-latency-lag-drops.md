# User Story US07-02 — Dashboards for latency, lag, and drops

## Description
As an SRE, I want dashboards for latency, lag, and drop metrics so that I can quickly visualize system health.

## Components
- Frontend: Grafana or similar dashboards.
- Infra: Dashboard provisioning as code.

## Acceptance Criteria
- Dashboards show p50, p95, and p99 latency for each stage.
- Consumer lag and drop/slow-consumer metrics are visible per consumer group.
- Dashboards support filtering by tenant, venue, and environment.
