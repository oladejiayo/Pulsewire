# User Story US07-04 — Control plane tracing with OpenTelemetry

## Description
As an SRE, I want distributed tracing for the control plane so that I can follow admin operations across services.

## Components
- Backend: OpenTelemetry tracing integrated into control plane services.
- Infra: Trace backend (for example, Jaeger or Tempo).

## Acceptance Criteria
- Control plane requests produce traces with spans for key operations.
- Traces include relevant tags such as user, tenant, and affected resources.
- Traces can be searched by operation name and error status.
