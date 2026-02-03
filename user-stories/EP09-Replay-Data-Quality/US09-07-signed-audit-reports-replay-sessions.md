# User Story US09-07 — Signed audit reports for replay sessions

## Description
As a Compliance Officer, I want signed audit reports for replay sessions so that I can provide evidence of what was replayed and by whom.

## Components
- Backend: Report generation and signing logic.
- Frontend: UI to download replay audit reports.

## Acceptance Criteria
- Replay reports include who requested the replay, when, what instruments and time range, and target topics.
- Reports are signed with a tamper-evident mechanism (for example, hash chain).
- Reports can be exported in a standard format (for example, PDF or JSON).
