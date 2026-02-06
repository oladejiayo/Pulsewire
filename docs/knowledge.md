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

## US01-02: Synthetic Exchange Feed Adapter

### Business Context

Developing and testing a market data pipeline requires realistic data without connecting to actual exchanges. A **synthetic exchange adapter** generates mock market data that:
- Simulates real exchange behavior for testing
- Runs independently of network connectivity
- Produces configurable load patterns for performance testing
- Generates both **trades** and **quotes** with realistic field values

### Market Data Message Types

#### Trades

A **trade** represents an executed transaction:

| Field | Type | Description |
|-------|------|-------------|
| symbol | String | Instrument identifier (e.g., "AAPL") |
| price | double | Execution price |
| quantity | long | Number of shares/contracts traded |
| timestamp | Instant | When the trade occurred |
| tradeId | String | Unique trade identifier |
| side | enum | BUY or SELL (aggressor side) |

Example: `AAPL traded 150 shares at $185.50`

#### Quotes (Top of Book)

A **quote** represents the current best bid/offer:

| Field | Type | Description |
|-------|------|-------------|
| symbol | String | Instrument identifier |
| bidPrice | double | Best bid price |
| bidSize | long | Quantity available at bid |
| askPrice | double | Best ask (offer) price |
| askSize | long | Quantity available at ask |
| timestamp | Instant | Quote timestamp |

Example: `AAPL bid 100@$185.48 / ask 200@$185.52`

### Configuration Parameters

The synthetic adapter must support:

| Parameter | Purpose | Default |
|-----------|---------|---------|
| symbols | List of instruments to generate | [AAPL, GOOGL, MSFT] |
| baseMessageRatePerSecond | Normal message rate | 10 |
| burstEnabled | Enable burst patterns | false |
| burstMultiplier | Rate multiplier during burst | 5x |
| burstDurationMs | Duration of each burst | 1000 |
| burstIntervalMs | Time between bursts | 10000 |
| tradeToQuoteRatio | Ratio of trades to quotes | 1:5 (more quotes) |

### Burst Pattern Behavior

Real markets exhibit **bursty** behavior (e.g., at market open, during news events):

```
Normal:    ──────────────────────────────────
Rate:      |    |    |    |    |    |    |
           10/s 10/s 10/s 10/s 10/s 10/s

Burst:     ──────────────────────────────────
Rate:      ||||| |    |    ||||| |    |    |
           50/s  10/s 10/s 50/s  10/s 10/s
           burst      normal     burst
```

### Realistic Price Generation

Prices should follow a **random walk** pattern:
- Each symbol maintains a baseline price
- Prices drift by small random amounts (±0.01% to ±0.1%)
- Bid/ask spread maintained (typically 1-5 cents for liquid stocks)
- Occasional larger moves for realism

### Start/Stop via Configuration

The adapter should support:
1. **Programmatic start/stop**: `connect()` / `disconnect()` methods
2. **Configuration-driven control**: Enabled/disabled via config file or API
3. **Graceful shutdown**: Complete in-flight messages before stopping

---
