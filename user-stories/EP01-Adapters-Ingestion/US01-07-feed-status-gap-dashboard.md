# User Story US01-07 — Feed status and gap dashboard widget

## Description
As an SRE, I want a dashboard widget that summarizes feed health and gap rates so that I can quickly identify problematic venues or instruments.

## Components
- Frontend: Dashboard widget in the admin UI.
- Backend: Aggregation endpoint exposing gap and error metrics per venue.

## Acceptance Criteria
- Dashboard lists all configured feeds with status (healthy/degraded/down).
- For each feed, the UI shows recent gap rate, last gap timestamp, and message rate.
- Widget can be filtered by venue and environment.
