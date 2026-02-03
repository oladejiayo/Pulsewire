# User Story US08-02 — OAuth2/JWT authentication for control plane

## Description
As an Admin, I want OAuth2/JWT authentication for the control plane so that only authorized users can change configuration.

## Components
- Backend: OAuth2 resource server configuration in the control plane.
- Frontend: Login flow integration.

## Acceptance Criteria
- Control plane APIs require valid JWTs with appropriate scopes.
- UI redirects unauthenticated users to the login flow.
- Tokens and sessions time out according to security policy.
