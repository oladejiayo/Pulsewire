# User Story US07-05 — Alert rules for key SLOs

## Description
As an SRE, I want alert rules for key SLOs so that I am notified when the system violates performance or reliability targets.

## Components
- Infra: Alert definitions in the monitoring stack.
- Docs: Alert playbook references.

## Acceptance Criteria
- Alerts exist for p99 latency breaches, gap rate spikes, consumer disconnect spikes, and GC regression.
- Alert thresholds and routing are documented.
- Alerts include links to relevant runbooks and dashboards.
