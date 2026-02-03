# User Story US05-05 — Backpressure and throttling policies at gateways

## Description
As a Platform Engineer, I want backpressure and throttling policies at gateways so that slow consumers do not degrade overall system health.

## Components
- Backend: Per-connection buffers and policies in gateway implementations.
- Infra: Configuration for thresholds and actions.

## Acceptance Criteria
- Gateways support configurable policies such as drop-oldest, drop-newest, downsampling, or disconnect.
- Metrics expose per-connection buffer utilization and drops.
- Policies and outcomes are documented and visible in logs.
