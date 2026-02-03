# User Story US01-05 — Emit raw events to raw.* streams

## Description
As a Normalizer Developer, I want all adapters to publish raw events to standardized raw.* streams so that downstream services can reliably consume and replay unmodified inputs.

## Components
- Backend: Integration from adapters into the distribution backbone abstraction.
- Infra: Topics/streams provisioning for raw events per venue.

## Acceptance Criteria
- All adapter implementations publish raw events onto raw.* streams using a shared schema.
- Raw events include source metadata, receive timestamp, and sequence identifiers.
- Raw streams are retained for at least the configured replay window.
