# User Story US04-02 — Implement in-memory backbone for MVP

## Description
As a Developer, I want an in-memory backbone implementation so that I can run the entire system locally without external brokers.

## Components
- Backend: In-memory implementation of the backbone abstraction.
- Infra: Configuration to select backbone implementation per environment.

## Acceptance Criteria
- In-memory backbone supports partitioned topics and fanout to multiple consumers.
- Implementation respects backpressure policies (for example, bounded queues).
- Local dev environment uses the in-memory backbone by default.
