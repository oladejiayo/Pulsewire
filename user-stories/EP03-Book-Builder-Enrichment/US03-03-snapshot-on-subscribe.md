# User Story US03-03 — Snapshot-on-subscribe and interval snapshots

## Description
As a Consumer, I want to receive a snapshot of the book on subscription (and optionally on a fixed interval) so that I can initialize local state correctly.

## Components
- Backend: Snapshot generation logic integrated with the book builder.
- Infra: Configuration for snapshot interval and depth.

## Acceptance Criteria
- On new subscription, a BOOK_SNAPSHOT is generated and sent before deltas.
- Optional periodic snapshots can be configured per instrument or feed.
- Snapshot generation does not block hot-path book updates beyond a defined budget.
