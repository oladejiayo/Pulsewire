# User Story US05-02 — Implement WebSocket gateway for UI consumers

## Description
As a UI Developer, I want a WebSocket gateway that streams market data snapshots and deltas so that browser-based dashboards can display real-time updates.

## Components
- Backend: WebSocket gateway service using Netty or Spring.
- Frontend: Example UI wiring to the WebSocket endpoint.

## Acceptance Criteria
- Authenticated clients can open a WebSocket connection and subscribe to instruments.
- Clients receive an initial snapshot followed by deltas according to the contract.
- Connection errors and backpressure events are surfaced to clients in a documented way.
