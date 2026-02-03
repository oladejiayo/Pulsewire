# User Story US01-01 — Define feed adapter SPI

## Description
As a Market Data Engineer, I want a clear Java SPI for feed adapters so that new exchange or vendor feeds can be onboarded consistently without changing the ingestion pipeline.

## Components
- Backend: Java interface definitions and base implementations for adapters.
- Infra: Build and packaging alignment for adapter modules.

## Acceptance Criteria
- A Java SPI exists for feed adapters covering connect, disconnect, heartbeat, message receive, and error callbacks.
- SPI supports multiple transport types (TCP, UDP, WebSocket, vendor SDK) without leaking transport details into downstream stages.
- Example implementation compiles and can be wired into the ingestion pipeline.
