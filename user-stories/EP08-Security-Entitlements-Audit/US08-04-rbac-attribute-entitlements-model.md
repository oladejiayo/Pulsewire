# User Story US08-04 — RBAC and attribute-based entitlements model

## Description
As a Compliance Officer, I want RBAC and attribute-based entitlements so that access can be controlled by venue, instrument, asset class, and depth.

## Components
- Backend: Entitlements service and data model.
- Docs: Policy definition and examples.

## Acceptance Criteria
- Roles and attributes can express entitlements by venue, asset class, instrument, and depth.
- Policies can be updated without service restarts.
- Policy evaluation is fast enough for low-latency subscription checks.
