# Feature — Deployment on Oracle Cloud Always Free

Status: **dropped by the owner on 2026-09-27** in favor of a desktop edition
that runs on each friend's machine (`desktop-edition.md`). Kept for reference
in case hosting comes back.

Goal: the owner's friend can use the vault on her own, at any time, over HTTPS,
from an Oracle Cloud Always Free ARM VM. The app stays private: it is shared
with one person and never advertised (`features/private-sharing.md`).

## Target architecture

```
Internet ──443──▶ Caddy (TLS, headers, rate limits)
                   ├── /                     → web app (static files built by Vite)
                   ├── /api, /oauth2, /login/oauth2, /logout → API :8090
                   ├── /auth/*               → Keycloak :8080 (admin console blocked)
                   └── files.<domain>        → MinIO :9000 (signed portrait URLs only)
Internal Docker network only: Postgres, Keycloak, MinIO, API.
```

- **One origin** for the web app, the API and Keycloak (`/auth`). That keeps
  the session cookie, CSRF and the Keycloak login on one site, the same as the
  Vite proxy does locally.
- **Only Caddy publishes ports** (80 and 443). Docker bypasses the host
  firewall for published ports, so no other service may publish one.
- **Portraits:** MinIO gets its own subdomain, because S3 signatures cover the
  host and the path, so it can't sit under a path prefix. The API already
  signs with `storage.public-endpoint`.

## Owner answers (2026-09-26)

1. **Domain:** DuckDNS. The owner hasn't used it, so the assistant gives
   step-by-step instructions when Phase 3 needs it.
2. **Redaction:** the owner wants the full text, as 5etools shows it, on the
   grounds that this is a non-commercial fan project that will never be
   advertised. The assistant explained that the risk is theirs to take, and
   offered options: keep redaction, show full text only for SRD entries, or
   show full text with the instance closed. **Waiting for the owner's pick.**
3. **Existing data:** the VM starts empty.
4. **Account:** Always Free only; the owner can't pay anything. No "Pay As You
   Go" upgrade. The risk that Oracle reclaims an idle VM is handled by
   off-VM backups and a one-command redeploy.

## Decisions for the owner (as first proposed)

1. **Domain.**
   - Recommended: a free DuckDNS name (`<name>.duckdns.org`). Its sub-names
     (`files.<name>.duckdns.org`) resolve to the same IP.
   - Alternative: a paid domain (about US$10 a year), which also allows
     Cloudflare in front of the VM.
2. **Catalogue redaction on the VM** (adr-0005 says on for any public
   deployment).
   - Recommended: keep it on. Mechanics still work; the prose shows markers.
   - Turning it off shows the full book text on a server on the internet.
3. **Existing data.** Start the VM empty (recommended), or copy the local
   characters over with a `pg_dump` and a MinIO copy.
4. **Account type.**
   - Oracle may reclaim an Always Free VM that stays idle for 7 days
     (CPU, network and memory all under 20%). A friend's occasional use can
     look idle.
   - Converting to "Pay As You Go" avoids reclaiming, and Always Free shapes
     stay free. It needs a budget alert (US$1) so nothing paid starts silently.

## Phase 0 — Oracle account (the owner, in the Oracle console)

- Sign up. The **home region can never be changed**: pick one with ARM
  capacity near Brazil (São Paulo or Vinhedo).
- Set up MFA on the Oracle account, and a US$1 budget alert.
- Create an SSH key pair on the development machine
  (`ssh-keygen -t ed25519`). Only the public key goes to Oracle.

## Phase 1 — Production setup in the repository (the assistant)

Nothing here runs on the VM yet, and local development keeps working.

1. **Configurable URLs.** A `prod` Spring profile reads every external address
   from the environment:
   - the Keycloak issuer, authorization, token, JWKS, user-info and end-session
     URLs, with an internal URL for the calls the API makes itself;
   - `storage.endpoint` (internal) and `storage.public-endpoint`
     (`https://files.<domain>`);
   - `server.forward-headers-strategy: native`, so redirects use the public
     host and `https`.
   - The session cookie stays `Secure`.
2. **API image.** `apps/api/Dockerfile`:
   - multi-stage: a Temurin 25 JDK to build, a JRE to run (both have arm64
     builds);
   - runs as a non-root user;
   - ships `content/` so the catalogue import can run inside the container.
3. **Keycloak image.** A small `Dockerfile` based on `keycloak:26.4`:
   - the login theme is baked in, and `kc.sh build` runs at image build time;
   - it starts with `start --optimized` (never `start-dev`);
   - `KC_HTTP_RELATIVE_PATH=/auth`, `KC_PROXY_HEADERS=xforwarded`,
     `KC_HOSTNAME=https://<domain>/auth`;
   - plain HTTP only inside the Docker network.
4. **Caddy image.** `caddy:2` rebuilt with the `caddy-ratelimit` plugin (via
   `xcaddy`), plus a `Caddyfile` that:
   - gets and renews the Let's Encrypt certificates on its own;
   - serves the built web app, and routes the paths in the diagram above;
   - sets HSTS, a strict CSP, `X-Content-Type-Options`, `Referrer-Policy` and
     `frame-ancestors 'none'`;
   - blocks `/auth/admin`, `/actuator` and the Keycloak metrics from outside;
   - rate-limits Keycloak's registration and login endpoints per IP. This is
     the open item "bots creating accounts in bulk" in `open-items.md`.
5. **`docker-compose.prod.yml`:**
   - pinned image tags (no `latest`, including MinIO);
   - one internal network, and ports published only by Caddy;
   - health checks, `restart: unless-stopped` and memory limits;
   - named volumes.
6. **Keycloak setup script** (`infra/deploy/keycloak-setup.sh`), run once and
   safe to repeat. It uses `kcadm.sh` to:
   - set the realm's redirect and post-logout URIs to the domain;
   - set the BFF client secret from `.env`;
   - create a permanent admin and delete the bootstrap admin
     (`tech-stack.md`, "Before exposing anything publicly").
7. **Secrets script** (`infra/deploy/generate-env.sh`): writes a production
   `.env` with long random values for every password and secret. The file gets
   `chmod 600` and is never committed.
8. **Backups** (`infra/deploy/backup.sh` plus a systemd timer):
   - nightly: a `pg_dump` of both databases, and an archive of the MinIO data;
   - kept for 7 days;
   - copied to OCI Object Storage, which is part of Always Free.
   - A restore is rehearsed once in Phase 4.
9. **Deploy script** (`infra/deploy/update.sh`): pulls the code, rebuilds the
   images on the VM, which builds natively for arm64, and restarts with no
   manual steps.
10. **New dependencies to record in `tech-stack.md`:** Caddy with
    `caddy-ratelimit`, and the Keycloak, API and Caddy images.

## Phase 2 — VM and hardening (the owner runs the commands; the assistant writes them)

- **Instance:** `VM.Standard.A1.Flex`, Ubuntu 24.04 (aarch64), 2 OCPU and
  12 GB. That leaves half of the free quota as a buffer. Boot volume 50 GB.
  - If the region reports "out of capacity", retry later or pick another
    availability domain.
- **Network:**
  - the security list allows 22 only from the owner's IP, plus 80 and 443;
  - Oracle's Ubuntu images also carry their own `iptables` rules, which must
    open 80 and 443 too.
- **SSH:**
  - key only; password login and root login disabled;
  - `fail2ban` on SSH.
- **System:** `unattended-upgrades` for security patches, a 4 GB swap file, and
  the timezone set.
- **Docker:** installed from Docker's own repository. A non-root `deploy` user
  runs the stack.

## Phase 3 — First deployment

1. Point the DuckDNS name at the VM's public IP.
2. Clone the repository on the VM, generate `.env`, and build the images.
3. `docker compose -f docker-compose.prod.yml up -d`.
4. Run the catalogue import once inside the API container.
5. Run the Keycloak setup script.

## Phase 4 — Verification before sharing the link

- **Security:**
  - SSL Labs grade A or better;
  - security headers present;
  - `/auth/admin` and `/actuator` unreachable from outside;
  - a port scan shows only 80 and 443 open (22 only from the owner's IP).
- **Behavior:** sign-up, login, create and finish a character, upload a
  portrait, roll dice, and log out, all over the public URL.
- **Rate limit:** repeated sign-ups from one IP get blocked.
- **Backups:** one backup and one restore, rehearsed on a copy.
- **Docs:** `changelog.md`, `tech-stack.md`, `architecture.md` and this file
  are updated.

## Operations afterwards

- **Updating:** `infra/deploy/update.sh` on the VM, after the owner pushes to
  GitHub.
- **Sign-up:** once the friend has an account, the owner can turn registration
  off (`registrationAllowed: false`). `open-items.md` tracks that decision.
- **Keycloak admin:** reached only through an SSH tunnel
  (`ssh -L 8080:localhost:8080`), never through the public URL.
