# Pulsewire
Java-based, event-driven, low-latency platform for ingesting, normalizing, enriching, and distributing real-time market data (ticks, trades, quotes, L1/L2 order books) to internal services and external consumers with strict latency SLOs.


It is designed to demonstrate:

- Event-driven architecture with pluggable messaging backbones (Solace PubSub+ class systems, Kafka-like logs, AMQP brokers)

- Low-latency data-plane engineering (concurrency, lock-minimization, backpressure, batching, zero-copy patterns)

- Production-grade reliability, observability, entitlement/security controls
