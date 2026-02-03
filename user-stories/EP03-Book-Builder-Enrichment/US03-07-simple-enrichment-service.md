# User Story US03-07 — Simple enrichment service for derived fields

## Description
As a Consumer, I want certain derived fields (for example, mid-price, spread, or liquidity flags) so that I can use enriched data without re-computing it.

## Components
- Backend: Enrichment microservice reading canonical/book events and publishing enriched events.
- Infra: Topics for enriched events and configuration for which enrichments are enabled.

## Acceptance Criteria
- Enrichment service computes at least one derived field (for example, mid-price) per instrument.
- Enriched events clearly indicate enrichment status and source fields.
- Enrichment latency is measured and stays within allocated budget.
