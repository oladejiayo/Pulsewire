# User Story US05-04 — Define TCP binary protocol and stub server

## Description
As an Ultra-Low Latency Consumer, I want a binary TCP protocol so that I can receive market data with minimal overhead.

## Components
- Backend: Protocol specification and minimal server implementation.
- Docs: Wire format documentation and sample client.

## Acceptance Criteria
- Binary protocol specifies message framing, encoding, and heartbeat behavior.
- Stub server accepts connections, authenticates clients, and streams basic events.
- Protocol is documented, including expected latency characteristics and trade-offs.
