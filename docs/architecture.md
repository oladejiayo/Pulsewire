# PulseWire Architecture Overview

This document provides a high-level architecture for PulseWire based on the PRD and maps major components to implementation epics.

## Logical Components

- Feed Adapter Services (EP01)
- Normalizer Service (EP02)
- Book Builder and Enrichment Services (EP03)
- Distribution Backbone (EP04)
- Gateways: WebSocket, gRPC, TCP binary (EP05)
- Control Plane & Admin UI (EP06)
- Observability & SRE Tooling (EP07)
- Security, Entitlements, and Audit Services (EP08)
- Replay and Data Quality Services (EP09)

## Event Flow

1. Feed adapters ingest vendor or synthetic feeds and publish raw events to `raw.*` streams.
2. The normalizer consumes raw events, applies schema-based validation and mapping, and publishes canonical events.
3. The book builder maintains per-instrument books and emits book snapshots and deltas.
4. The distribution backbone handles partitioned, ordered delivery of canonical and book events.
5. Gateways expose subscription APIs to internal services and UIs, enforcing backpressure and entitlements.
6. A persistent store captures raw and optionally canonical events for replay and audit.
7. Observability pipelines export metrics, logs, and traces to monitoring backends.
8. Control plane services manage configuration, schemas, tenants, and feature flags.

## Deployment Considerations

- Services are containerized and deployed to Kubernetes with horizontal scaling by partition.
- Environments include local dev (with in-memory backbone), integration, staging, and production.
- mTLS is used for internal communication, and OAuth2/JWT secures control plane APIs.
- Metrics and traces feed into dashboards and alerts aligned with latency SLOs.
