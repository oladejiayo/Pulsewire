# User Story US04-03 — Partitioning by instrument and consistent hashing

## Description
As a Performance Engineer, I want partitioning by instrument using consistent hashing so that load can be distributed evenly across workers.

## Components
- Backend: Partitioning logic integrated into the backbone.
- Infra: Configuration for number of partitions and assignment strategy.

## Acceptance Criteria
- Partition key is based on instrument_id (or venue+instrument) as defined in the PRD.
- Consistent hashing spreads instruments across partitions with minimal movement when partitions change.
- Partition assignment can be inspected and debugged via admin endpoints.
