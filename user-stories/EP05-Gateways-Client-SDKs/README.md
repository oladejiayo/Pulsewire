# Epic EP05 — Gateways & Client SDKs

Scope: FR4 (client gateways), journeys for consumer subscriptions, and backpressure strategies.

Goal: Provide WebSocket, gRPC, and TCP gateways plus lightweight client SDKs so that services and UIs can subscribe to snapshots and deltas with clear contracts and backpressure behavior.

User stories:

- US05-01 — Design subscription API and payload contracts
- US05-02 — Implement WebSocket gateway for UI consumers
- US05-03 — Implement gRPC streaming gateway for services
- US05-04 — Define TCP binary protocol and stub server
- US05-05 — Backpressure and throttling policies at gateways
- US05-06 — Sample Java client SDK
- US05-07 — Sample UI client for trades and book views
