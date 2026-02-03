# User Story US02-03 — Implement normalizer service for TRADE and QUOTE

## Description
As a Normalizer Developer, I want a service that converts raw trade and quote messages into canonical events so that downstream consumers see consistent data.

## Components
- Backend: Normalizer microservice consuming raw.* streams and producing canonical events.
- Infra: Topics/streams provisioning for canonical.events.

## Acceptance Criteria
- Service consumes raw events from at least one adapter and publishes canonical TRADE and QUOTE events.
- Normalization logic maps vendor-specific fields to canonical fields using configuration.
- Failed normalizations are counted, logged, and surfaced as metrics without crashing the pipeline.
