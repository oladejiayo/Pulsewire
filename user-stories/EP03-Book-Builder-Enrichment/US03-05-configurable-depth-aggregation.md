# User Story US03-05 — Configurable depth and aggregation rules

## Description
As a Platform Engineer, I want configurable book depth and aggregation rules so that different consumers can receive appropriately sized books.

## Components
- Backend: Book builder configuration for depth and price aggregation.
- Frontend: Admin UI to configure depth profiles per tenant or venue.

## Acceptance Criteria
- Depth configuration supports per-tenant or per-feed settings.
- Aggregation rules (e.g., price bucketing) can be adjusted without code changes.
- Changes are auditable and take effect without full system restarts.
