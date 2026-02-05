# PulseWire Business Knowledge

This document captures business domain knowledge, terminology, and data flows for the PulseWire market data platform.

---

## US01-01: Feed Adapter SPI

### Business Context

Market data originates from various **exchanges** and **data vendors** (e.g., NYSE, NASDAQ, CME, Bloomberg, Reuters). Each source has unique:
- **Transport protocols**: TCP, UDP multicast, WebSocket, proprietary SDKs
- **Message formats**: FIX, ITCH, proprietary binary, JSON
- **Connection semantics**: Persistent connections, session management, heartbeats

The **Feed Adapter** abstraction decouples the ingestion pipeline from these vendor-specific details, enabling:
1. **Consistent onboarding**: New feeds follow the same SPI contract
2. **Testability**: Mock adapters for integration testing
3. **Operational uniformity**: Same monitoring, lifecycle, and error handling patterns

### Lifecycle States

```
    ┌─────────────┐
    │ DISCONNECTED│◄────────────────┐
    └──────┬──────┘                 │
           │ connect()              │ error / disconnect()
           ▼                        │
    ┌─────────────┐                 │
    │ CONNECTING  │                 │
    └──────┬──────┘                 │
           │ onConnected()          │
           ▼                        │
    ┌─────────────┐                 │
    │  CONNECTED  │─────────────────┘
    └──────┬──────┘
           │ messages flow
           ▼
    FeedMessageHandler.onMessage()
```

### Key Callbacks

| Callback | Purpose |
|----------|---------|
| `connect()` | Initiate connection to feed source |
| `disconnect()` | Gracefully close connection |
| `onConnected()` | Invoked when connection is established |
| `onDisconnected(reason)` | Invoked when connection drops |
| `onMessage(rawBytes)` | Raw message received from feed |
| `onError(exception)` | Error during connection or message processing |
| `sendHeartbeat()` | Maintain connection liveness |
| `onHeartbeatTimeout()` | Heartbeat response not received in time |

### Transport Abstraction

The SPI must **not leak** transport details to downstream stages. The normalizer receives:
- Raw bytes or decoded objects
- Metadata: source ID, receive timestamp, sequence number

Transport types are declared for configuration/metrics but don't affect the message flow interface.

### Error Recovery Strategy

1. **Transient errors**: Exponential backoff reconnection
2. **Fatal errors**: Alert, stop adapter, require manual intervention
3. **Gap detection**: Request replay if sequence gap detected (vendor-specific)

---
