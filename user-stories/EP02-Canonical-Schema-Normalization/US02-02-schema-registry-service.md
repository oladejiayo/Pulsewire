# User Story US02-02 — Implement schema registry service (basic)

## Description
As a Platform Engineer, I want a basic schema registry service so that producers and consumers can resolve schema versions and validate messages.

## Components
- Backend: Spring Boot schema registry service with CRUD APIs.
- Frontend: Simple admin UI to browse schemas and versions.

## Acceptance Criteria
- Registry exposes APIs to register, retrieve, and list schemas and their versions.
- Service persists schemas durably (e.g., PostgreSQL).
- Admin UI shows schema name, version, and last modification time.
