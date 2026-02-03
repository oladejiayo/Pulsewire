# User Story US08-05 — Entitlement enforcement in subscription and fanout

## Description
As a Compliance Officer, I want entitlements enforced at both subscription and fanout so that unauthorized data is never delivered.

## Components
- Backend: Checks in gateways and backbone fanout.
- Infra: Logging and metrics for entitlement decisions.

## Acceptance Criteria
- Unauthorized subscription requests are rejected with clear error responses.
- Entitlement checks also run at fanout, ensuring defense in depth.
- All entitlement decisions are logged with subject, request parameters, and outcome.
