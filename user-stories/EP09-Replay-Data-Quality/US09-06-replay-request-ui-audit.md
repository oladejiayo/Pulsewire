# User Story US09-06 — Replay request UI and audit integration

## Description
As a Compliance Officer, I want a UI to request and review replays so that I can support investigations.

## Components
- Frontend: Admin UI for creating and monitoring replay jobs.
- Backend: APIs for replay job submission and status.

## Acceptance Criteria
- Users can request replays specifying instruments, time range, and target environment.
- Replay jobs are listed with status, progress, and outputs.
- All replay requests are recorded in the audit log with user identity.
