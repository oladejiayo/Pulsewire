# User Story US01-06 — Expose adapter lifecycle controls in control plane

## Description
As a Platform Engineer, I want to start, stop, and restart feed adapters from a central control plane so that I can manage ingestion without logging into individual pods.

## Components
- Backend: Control plane APIs to manage adapter lifecycle.
- Frontend: Admin UI controls for adapter status and actions.

## Acceptance Criteria
- Control plane exposes authenticated endpoints to start, stop, and restart each adapter instance.
- Admin UI shows current status, last error, and last restart time for each adapter.
- Lifecycle actions are audited with user identity and timestamp.
