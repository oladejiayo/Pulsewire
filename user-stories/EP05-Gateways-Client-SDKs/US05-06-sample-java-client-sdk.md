# User Story US05-06 — Sample Java client SDK

## Description
As a Consumer Team, I want a sample Java SDK so that I can quickly integrate with PulseWire gateways.

## Components
- Backend: Java library wrapping WebSocket and gRPC gateway APIs.
- Docs: Quickstart guide for using the SDK.

## Acceptance Criteria
- SDK exposes simple methods to authenticate, subscribe, and receive events.
- Example project demonstrates subscribing to trades and quotes using the SDK.
- SDK handles reconnects and backpressure signals according to documented behavior.
