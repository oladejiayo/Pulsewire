# Epic EP03 — Book Builder & Enrichment

Scope: FR3, parts of the canonical data model and enrichment path.

Goal: Build deterministic L2 order books and enrichment services that publish book snapshots and deltas with configurable depth and aggregation.

User stories:

- US03-01 — Define book event schema for snapshots and deltas
- US03-02 — Implement in-memory L2 order book per instrument
- US03-03 — Snapshot-on-subscribe and interval snapshots
- US03-04 — Publish book deltas after snapshots
- US03-05 — Configurable depth and aggregation rules
- US03-06 — Golden tests for deterministic book reconstruction
- US03-07 — Simple enrichment service for derived fields
