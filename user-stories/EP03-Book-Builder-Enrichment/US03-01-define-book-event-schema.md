# User Story US03-01 — Define book event schema for snapshots and deltas

## Description
As a Canonical Schema Engineer, I want schemas for book snapshots and deltas so that order book state can be reconstructed deterministically.

## Components
- Backend: Schema definitions for BOOK_SNAPSHOT and BOOK_DELTA events.
- Docs: Field definitions and examples for multi-level books.

## Acceptance Criteria
- BOOK_SNAPSHOT schema supports levels with price, size, side, and level index.
- BOOK_DELTA schema supports add, update, and remove operations.
- Schemas are registered in the schema registry with versioning.
