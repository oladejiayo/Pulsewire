# User Story US03-02 — Implement in-memory L2 order book per instrument

## Description
As a Book Builder Developer, I want an in-memory L2 order book per instrument so that I can maintain up-to-date depth for each market.

## Components
- Backend: Order book component implementing add/update/remove operations.
- Infra: Configuration for maximum depth and memory usage.

## Acceptance Criteria
- Order book supports applying BOOK_DELTA events and produces a consistent state.
- Implementation follows single-writer principles per instrument/partition.
- Unit tests cover typical and edge-case deltas (e.g., out-of-order updates).
