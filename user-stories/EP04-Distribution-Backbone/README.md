# Epic EP04 — Distribution Backbone

Scope: FR4 (internal backbone), partitioning model, and low-latency data-plane design.

Goal: Provide a pluggable distribution backbone abstraction with both brokered pub/sub and partitioned log implementations, ensuring per-instrument ordering and scalable fanout.

User stories:

- US04-01 — Define distribution backbone abstraction
- US04-02 — Implement in-memory backbone for MVP
- US04-03 — Partitioning by instrument and consistent hashing
- US04-04 — Ordering guarantees and delivery semantics
- US04-05 — Durable log-backed backbone implementation (generic)
- US04-06 — Metrics for queue depth and consumer lag
- US04-07 — Partition assignment and scaling controls
- US04-08 — Implement Kafka-backed backbone integration
