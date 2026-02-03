# Epic EP01 — Adapters & Ingestion

Scope: FR1, parts of FR6, MVP feed adapters, and ingestion portions of the data plane.

Goal: Provide pluggable Java-based feed adapters that ingest raw market data from multiple sources, track gaps, timestamp events, and emit raw streams for downstream normalization and replay.

User stories:

- US01-01 — Define feed adapter SPI
- US01-02 — Implement synthetic exchange feed adapter
- US01-03 — Implement WebSocket market data adapter
- US01-04 — Implement sequence tracking and gap detection
- US01-05 — Emit raw events to raw.* streams
- US01-06 — Expose adapter lifecycle controls in control plane
- US01-07 — Feed status and gap dashboard widget
