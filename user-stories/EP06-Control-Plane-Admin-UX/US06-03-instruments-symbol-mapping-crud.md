# User Story US06-03 — Instruments master and symbol mapping CRUD

## Description
As a Market Data Engineer, I want to manage instrument definitions and symbol mappings so that feeds can be normalized to a shared instrument_id.

## Components
- Backend: CRUD APIs for instruments and symbol mappings.
- Frontend: Admin UI for searching and editing instruments.

## Acceptance Criteria
- Instruments include fields such as instrument_id, venue, symbol, asset class, and currency.
- Symbol mappings from vendor symbols to instrument_id are manageable via UI.
- Changes propagate to normalization and book builder services without code changes.
