# User Story US08-01 — Service-to-service mTLS

## Description
As a Security Engineer, I want mTLS between services so that inter-service communication is authenticated and encrypted.

## Components
- Infra: PKI, certificate issuance, and rotation.
- Backend: Service configurations and libraries for mTLS.

## Acceptance Criteria
- All internal service-to-service calls use mTLS.
- Certificates are rotated without downtime.
- Misconfigured certificates result in clear errors and alerts.
