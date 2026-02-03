# User Story US03-04 — Publish book deltas after snapshots

## Description
As a Consumer, I want efficient book deltas after an initial snapshot so that I can keep my view up-to-date with minimal bandwidth.

## Components
- Backend: Delta emission pipeline integrated with the distribution backbone.
- Infra: Topics/streams for book.events.

## Acceptance Criteria
- After a snapshot, only BOOK_DELTA events are emitted for a given subscription.
- Deltas are ordered and sufficient to reconstruct the book state.
- Book delta stream is partitioned consistently with canonical events.
