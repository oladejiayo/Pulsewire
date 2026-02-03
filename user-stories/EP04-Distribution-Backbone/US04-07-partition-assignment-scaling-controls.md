# User Story US04-07 — Partition assignment and scaling controls

## Description
As a Platform Engineer, I want coordinated partition assignment and scaling controls so that I can scale the cluster without breaking ordering.

## Components
- Backend: Coordinator service or logic for assigning partitions to workers.
- Frontend: Admin UI or CLI to trigger rebalancing and review assignments.

## Acceptance Criteria
- Partition assignments can be listed and updated via an authenticated control API.
- Rebalancing avoids double-processing beyond documented semantics.
- Scaling events are logged and auditable.
