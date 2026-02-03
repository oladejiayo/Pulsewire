# User Story US08-06 — Immutable audit log for admin and replay actions

## Description
As a Compliance Officer, I want an immutable audit log so that I can see who changed what and when, and who replayed data.

## Components
- Backend: Append-only audit log storage and API.
- Frontend: UI for searching and exporting audit records.

## Acceptance Criteria
- Audit log records config changes, entitlement changes, replay requests, and admin actions.
- Audit entries include actor, action, timestamp, and context.
- Audit data can be exported with integrity checks (for example, hash chain).
