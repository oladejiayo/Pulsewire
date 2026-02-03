# User Story US04-04 — Ordering guarantees and delivery semantics

## Description
As a Consumer, I want clear ordering and delivery semantics so that I know how to handle duplicates or missing messages.

## Components
- Backend: Delivery modes (at-most-once, at-least-once) in backbone implementation.
- Docs: Documentation for delivery guarantees and trade-offs.

## Acceptance Criteria
- Backbone supports at least at-most-once and at-least-once semantics.
- Ordering is guaranteed per partition and documented for consumers.
- Delivery mode per stream can be configured and observed via metrics.
