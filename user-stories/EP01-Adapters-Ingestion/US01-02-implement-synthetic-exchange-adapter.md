# User Story US01-02 — Implement synthetic exchange feed adapter

## Description
As a Market Data Engineer, I want a synthetic exchange feed adapter that publishes realistic trade and quote events so I can develop and test the pipeline without connecting to a real venue.

## Components
- Backend: Synthetic adapter implementation using the feed adapter SPI.
- Infra: Configurable rates and instruments via config service.

## Acceptance Criteria
- Synthetic adapter can be started and stopped independently via configuration.
- Adapter emits trade and quote messages with realistic fields and sequence numbers.
- Adapter configuration supports symbol list, message rate, and burst patterns.
