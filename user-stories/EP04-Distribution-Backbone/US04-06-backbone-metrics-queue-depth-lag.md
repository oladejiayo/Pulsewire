# User Story US04-06 — Metrics for queue depth and consumer lag

## Description
As an SRE, I want metrics for queue depth and consumer lag so that I can identify bottlenecks in the backbone.

## Components
- Backend: Metrics instrumentation in backbone producers and consumers.
- Infra: Export to Prometheus and visualization in dashboards.

## Acceptance Criteria
- Per-topic/partition queue depths and consumer lag metrics are exposed.
- Dashboards show distribution of lag across consumers.
- Alerts can be configured on lag and queue depth thresholds.
