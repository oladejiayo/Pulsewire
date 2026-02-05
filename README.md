# Pulsewire
Java-based, event-driven, low-latency platform for ingesting, normalizing, enriching, and distributing real-time market data (ticks, trades, quotes, L1/L2 order books) to internal services and external consumers with strict latency SLOs.


It is designed to demonstrate:

- Event-driven architecture with pluggable messaging backbones (Solace PubSub+ class systems, Kafka-like logs, AMQP brokers)

- Low-latency data-plane engineering (concurrency, lock-minimization, backpressure, batching, zero-copy patterns)

- Production-grade reliability, observability, entitlement/security controls

## Project layout

- `pom.xml` — Maven parent (Java 21, Spring Boot 3.3.x) and module definitions.
- `pulsewire-core` — Core domain models (`MarketEvent`, `Trade`, `Quote`), backbone abstractions (`BackbonePublisher`, `BackboneConsumer`), and implementations (in-memory, Kafka).
- `pulsewire-control-plane` — Spring Boot control plane service with REST APIs for instruments, feeds, subscriptions, and PostgreSQL persistence.
- `pulsewire-data-plane` — Data-plane service: feed adapters, normalizer, and WebSocket gateway for streaming market data.
- `pulsewire-frontend` — React + TypeScript admin dashboard with real-time market data visualization.
- `docker-compose.yml` — Compose file to run all services with Kafka and PostgreSQL.
- `docs/` — Architecture and documentation.
- `user-stories/` — Epics and implementation user stories derived from the PRD.

## Prerequisites

- Java 21
- Maven 3.8+ installed locally (`mvn` on your PATH)
- Node.js 18+ (for frontend development)
- Docker (for running with Kafka/PostgreSQL)

## Quick Start (PowerShell)

The easiest way to run PulseWire is with the included startup script:

```powershell
# Local mode (in-memory backbone, H2 database - no Docker required)
.\start-pulsewire.ps1

# Full mode (Kafka + PostgreSQL via Docker)
.\start-pulsewire.ps1 -Mode full

# Infrastructure only (start Docker services without apps)
.\start-pulsewire.ps1 -Mode infra-only

# Build only
.\start-pulsewire.ps1 -Mode build-only

# Clean build
.\start-pulsewire.ps1 -Clean

# Skip frontend (backend only)
.\start-pulsewire.ps1 -NoFrontend
```

The script will:
1. Check prerequisites (Java, Maven, Node.js, Docker)
2. Build the project
3. Start infrastructure (if using full mode)
4. Launch Control Plane, Data Plane, and Frontend services
5. Display endpoints when ready

**Endpoints after startup:**
- Frontend Dashboard: http://localhost:3000
- Control Plane API: http://localhost:8080
- Data Plane: http://localhost:8081
- WebSocket: ws://localhost:8081/ws/market-data

## Build and run locally (in-memory backbone)

From the repository root:

```bash
mvn clean package -DskipTests
```

Run control plane (uses H2 in-memory database):

```bash
mvn -pl pulsewire-control-plane spring-boot:run
```

Run data plane (uses in-memory backbone):

```bash
mvn -pl pulsewire-data-plane spring-boot:run
```

Endpoints:

- Control Plane: http://localhost:8080
  - `GET /api/health` — health check
  - `GET /api/instruments` — list instruments
  - `POST /api/instruments` — create instrument
  - `GET /api/feeds` — list feeds
  - `GET /api/subscriptions` — list subscriptions
  - `/h2-console` — H2 database console (dev only)
- Data Plane: http://localhost:8081
  - `ws://localhost:8081/ws/market-data` — WebSocket endpoint for streaming

## Run with Docker Compose (Kafka + PostgreSQL)

Build images and start all services:

```bash
docker compose up --build
```

Services:

- `postgres` — PostgreSQL database on port 5432
- `zookeeper` — Kafka dependency on port 2181
- `kafka` — Kafka broker on port 9092
- `control-plane` — http://localhost:8080 (connected to PostgreSQL)
- `data-plane` — http://localhost:8081 (connected to Kafka)

Stop everything:

```bash
docker compose down -v
```

## Running tests

```bash
mvn test
```

## Frontend Development

The frontend is a React + TypeScript application built with Vite.

```bash
cd pulsewire-frontend
npm install
npm run dev
```

Features:
- **Dashboard** — Real-time market data visualization with WebSocket streaming
- **Instruments** — CRUD management for tradeable instruments
- **Feeds** — Configure market data feed sources
- **Subscriptions** — Link instruments to feeds with priority settings

The frontend proxies API requests to the backend services:
- `/api/*` → Control Plane (http://localhost:8080)
- `/ws/*` → Data Plane WebSocket (ws://localhost:8081)
