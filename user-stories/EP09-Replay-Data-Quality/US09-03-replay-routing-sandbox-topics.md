# User Story US09-03 — Replay routing to sandbox topics

## Description
As a Consumer, I want replayed events to be routed to sandbox topics so that they do not interfere with live production streams.

## Components
- Backend: Target routing configuration in the replay service.
- Infra: Provisioning of sandbox topics/streams.

## Acceptance Criteria
- Replay jobs can target sandbox topics distinct from live topics.
- Consumers can subscribe to sandbox topics using the same contracts as live streams.
- Routing choices are recorded in audit logs.
