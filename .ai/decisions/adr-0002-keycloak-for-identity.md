# ADR 0002 — Keycloak as the identity provider

**Status:** accepted

## Context

The application needs authentication, and eventually registration, password reset,
email verification and possibly social login. Options were hand-rolled JWT
authentication inside the Spring application, a managed identity service, or a
self-hosted identity provider.

## Decision

Keycloak, running as a container beside the application, with the API acting purely
as an OAuth2 resource server.

## Rationale

- The API never handles credentials. It validates token signatures against Keycloak's
  public keys, cached from the JWKS endpoint, so there is no per-request lookup and no
  password logic in application code.
- Registration, password reset, MFA, sessions and social login come configured rather
  than written.
- Keycloak is standard OIDC, so the backend is not coupled to it: switching providers
  is a change of `issuer-uri` plus the one class that maps token claims to Spring
  authorities.
- Operating a real identity provider is a deliberate secondary goal for the owner.

## Consequences

- One more container, with its own database inside the same PostgreSQL instance.
- Realm configuration lives outside the code, so **the realm must be exported to
  `infra/keycloak/` and committed**. Without that, the configuration exists only
  inside a Docker volume and is lost when the volume is removed.
- The deployment target must run an always-on process, which rules out serverless and
  sleep-on-idle free tiers. This is why deployment targets a single VM.

## Rejected alternatives

**Hand-rolled JWT in Spring Security** — cheaper to start, but every identity feature
becomes application code, and credential handling becomes the project's problem.

**Firebase Auth** — the least work, but it cannot federate into a standard OIDC setup
as a third-party provider, and it would leave the project with a cloud dependency for
one narrow purpose.