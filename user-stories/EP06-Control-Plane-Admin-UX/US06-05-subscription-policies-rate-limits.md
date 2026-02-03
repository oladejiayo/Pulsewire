# User Story US06-05 — Subscription policies and rate limits

## Description
As a Platform Engineer, I want to define subscription policies and rate limits so that consumer access can be controlled centrally.

## Components
- Backend: Policy model and APIs for subscription constraints and rate limits.
- Frontend: Admin UI to configure and inspect policies.

## Acceptance Criteria
- Policies support constraints by tenant, venue, asset class, and instrument.
- Rate limits can be defined per tenant or API key and enforced at gateways.
- Violations are logged and surfaced via metrics.
