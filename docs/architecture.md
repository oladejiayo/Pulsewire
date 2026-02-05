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

---

## US01-01: Feed Adapter SPI Architecture

### Design Decisions

**ADR-001: Callback-based SPI over polling**
- *Context*: Feed adapters need to notify the pipeline of events (connect, message, error)
- *Decision*: Use callback interfaces (`FeedEventHandler`) rather than polling
- *Rationale*: Lower latency, natural fit for async I/O, simpler backpressure propagation

**ADR-002: Transport type as metadata, not interface**
- *Context*: Different transports (TCP, UDP, WebSocket, SDK) have different characteristics
- *Decision*: Declare transport type via enum; don't expose transport-specific APIs
- *Rationale*: Downstream stages remain transport-agnostic; metrics can be tagged by transport

**ADR-003: Separate lifecycle from message handling**
- *Context*: Connection management and message processing are distinct concerns
- *Decision*: `FeedAdapter` handles lifecycle; `FeedEventHandler` receives callbacks
- *Rationale*: Cleaner separation of concerns; easier testing and composition

### Class Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        <<interface>>                            │
│                         FeedAdapter                             │
├─────────────────────────────────────────────────────────────────┤
│ + getId(): String                                               │
│ + getTransportType(): TransportType                             │
│ + connect(handler: FeedEventHandler): void                      │
│ + disconnect(): void                                            │
│ + isConnected(): boolean                                        │
│ + sendHeartbeat(): void                                         │
└─────────────────────────────────────────────────────────────────┘
                              △
                              │ implements
          ┌───────────────────┼───────────────────┐
          │                   │                   │
┌─────────┴─────────┐ ┌───────┴───────┐ ┌────────┴────────┐
│ TcpFeedAdapter    │ │WebSocketAdapter│ │SyntheticAdapter │
└───────────────────┘ └───────────────┘ └─────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        <<interface>>                            │
│                       FeedEventHandler                          │
├─────────────────────────────────────────────────────────────────┤
│ + onConnected(adapterId: String): void                          │
│ + onDisconnected(adapterId: String, reason: String): void       │
│ + onMessage(adapterId: String, message: RawFeedMessage): void   │
│ + onError(adapterId: String, error: Throwable): void            │
│ + onHeartbeatTimeout(adapterId: String): void                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          <<enum>>                               │
│                        TransportType                            │
├─────────────────────────────────────────────────────────────────┤
│ TCP, UDP, WEBSOCKET, VENDOR_SDK                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       RawFeedMessage                            │
├─────────────────────────────────────────────────────────────────┤
│ - payload: byte[]                                               │
│ - receiveTimestamp: Instant                                     │
│ - sequenceNumber: long                                          │
└─────────────────────────────────────────────────────────────────┘
```

### Threading Model

- Each adapter manages its own I/O thread(s)
- Callbacks are invoked on the adapter's thread
- Downstream stages must handle thread handoff if needed
- `onMessage` must be non-blocking; offload heavy processing

### Error Handling

- Adapters catch transport exceptions and invoke `onError`
- Fatal errors trigger `disconnect()` and state transition to DISCONNECTED
- Reconnection logic is external to the SPI (handled by orchestrator)

