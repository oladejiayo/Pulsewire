# User Story US06-04 — Feed configuration management

## Description
As an Admin, I want to manage feed configuration centrally so that adapters can be controlled without changing code.

## Components
- Backend: APIs for feed configuration (endpoints, credentials, options).
- Frontend: UI pages for configuring feeds per environment.

## Acceptance Criteria
- Feed configurations can be created, updated, and rolled back via versioning.
- Control plane pushes configuration updates to adapter services via a secure channel.
- Configuration changes are audited and visible in history views.
