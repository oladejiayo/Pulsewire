# User Story US02-06 — Schema versioning and compatibility checks

## Description
As a Platform Engineer, I want schema versioning and compatibility checks so that new schema versions do not break existing consumers.

## Components
- Backend: Versioning model and compatibility rules in the schema registry.
- Docs: Guidance for introducing new schema versions.

## Acceptance Criteria
- Schema registry tracks major and minor versions for each schema.
- Service exposes an API to validate compatibility of a new schema against the previous version.
- Breaking changes require either a new topic or new major version and are rejected without an explicit override.
