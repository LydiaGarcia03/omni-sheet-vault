# Free deployment options

How to put omni-sheet-vault (Spring Boot + React + Keycloak + PostgreSQL +
MinIO, Docker) online for free, so other people open it through a link in the
browser instead of installing the desktop edition.

Researched on 2026-10-13. Free tiers change often: check each provider's
pricing page before relying on a limit quoted here.

Nothing in this document is implemented yet. The files it names
(`deploy/oracle/…`, `application-prod.yml`) do not exist until someone creates
them following `deployment-oracle-guide.md`.

---

## 1. Summary

| Option | Cost | Availability | App changes | Effort |
| --- | --- | --- | --- | --- |
| **A. Own PC + Cloudflare Tunnel** | $0 (own domain optional, ~US$ 10/year) | Only while the PC is on | Production layout (section 2) + tunnel | Low |
| **B. Oracle Cloud Always Free VM** | $0 (card required to verify the account) | 24/7 | Production layout (section 2) | Medium |
| **C. Render + Neon + managed services** | $0 | Sleeps after 15 min idle, ~1 min to wake | Replace Keycloak, MinIO and Postgres; API serves the web build | High |

- Keycloak needs at least ~1.5 GB of RAM, and no free managed host offers
  that. That is why A and B keep the whole stack in Docker on one machine.
- **A** fits when the app being offline sometimes is acceptable.
- **B** keeps the app online 24/7 on a free server; its step-by-step guide is
  `deployment-oracle-guide.md`.
- **C** is only worth it as a PaaS exercise.

---

## 2. Production layout (options A and B)

Production needs no Java code change. It mirrors local development:

| Local development | Production |
| --- | --- |
| Vite serves the web app on `5173` and proxies `/api`, `/oauth2`, `/login/oauth2`, `/logout` to the API | **Caddy** serves the built web app (`npm run build`) on `app.<host>` and proxies the same four paths to the API |
| Keycloak on `localhost:8081` | Keycloak on its own hostname `auth.<host>`, through Caddy |
| MinIO on `localhost:9000` | MinIO on its own hostname `files.<host>`, through Caddy |
| `dev` profile, `.env` | `prod` profile, `.env` on the server |

Why three hostnames:

- **Keycloak on its own host** keeps it at the root path (`/realms/...`) as
  in development, so the realm export and every URL keep their shape.
- **MinIO on its own host** is required because a presigned URL's signature
  covers the host and the path (`S3PortraitStorage` signs with
  `storage.public-endpoint`). Behind a path prefix the signature breaks.
- **The web app and the API share `app.<host>`**, so the session cookie and
  the CSRF cookie flow exactly as with Vite's proxy (adr-0008).

The browser uses the public `https://auth.<host>` address for login and
logout. The API reaches Keycloak over Docker's internal network
(`http://keycloak:8081`) for tokens and keys. This works because
`application.yml` already spells out each Keycloak endpoint instead of
discovering them from the issuer.

The public deployment keeps `app.catalogue.redaction-enabled: true` (the
default in `application.yml`, adr-0005). Only the `dev` and `desktop` profiles
turn it off.

---

## 3. Option A: own PC + Cloudflare Tunnel

The app runs on the owner's computer with the production files from
`deployment-oracle-guide.md` Phase 6. `cloudflared` opens an outbound connection to Cloudflare, which
publishes it over HTTPS. No router ports are opened and the home IP is not
exposed.

| Variant | Needs | Address | When |
| --- | --- | --- | --- |
| **A1. Quick Tunnel** | Nothing (no account) | Three random `*.trycloudflare.com` URLs, new on every run | One-off test with friends |
| **A2. Named tunnel** | Free Cloudflare account + own domain | `app.`, `auth.`, `files.` subdomains of your domain, fixed | Recurring use (recommended) |

How it differs from option B:

- The guide's Phases 1–5 (account, VM, ports, Docker on Linux, address) are
  replaced by Docker Desktop and the tunnel. Phases 6–13 apply as written,
  run locally instead of over SSH.
- Caddy listens on plain HTTP, because Cloudflare terminates HTTPS. In the
  Caddyfile, prefix every site address with `http://`, for example
  `http://{$APP_HOST}`.
- Docker Desktop must be running, and Windows sleep must be disabled while the
  app is online.

**A1. Quick Tunnel:**

1. `winget install --id Cloudflare.cloudflared`.
2. Run `cloudflared tunnel --url http://localhost:80` three times, each in
   its own terminal. Each run prints a different hostname.
3. Set `APP_HOST`, `AUTH_HOST` and `FILES_HOST` in `.env` to those three
   hostnames. Each tunnel forwards its public `Host` header, so Caddy routes
   every request to the right service.
4. Update the Keycloak client (`deployment-oracle-guide.md` Phase 9.3), then
   restart `keycloak`, `api` and `caddy`.

The URLs change on every run, so steps 3 and 4 repeat each time. Cloudflare
does not recommend Quick Tunnels for continuous use.

**A2. Named tunnel:**

1. Add your domain to a free Cloudflare account and switch the registrar's
   nameservers to Cloudflare's.
2. **Zero Trust → Networks → Tunnels → Create a tunnel** (*Cloudflared*), and
   copy the token.
3. Add this service to the compose, and `CF_TUNNEL_TOKEN` to `.env`:

   ```yaml
   cloudflared:
     image: cloudflare/cloudflared:2026.6.0
     command: tunnel --no-autoupdate run
     environment:
       TUNNEL_TOKEN: ${CF_TUNNEL_TOKEN}
     restart: unless-stopped
     depends_on: [caddy]
   ```

4. On the tunnel's **Public Hostnames** tab, route `app.<domain>`,
   `auth.<domain>` and `files.<domain>` to `HTTP` → `caddy:80`.
5. Optional: **Zero Trust → Access → Applications** can restrict
   `app.<domain>` to an allow-list of e-mails (the free plan covers 50 users).

---

## 4. Option B: Oracle Cloud Always Free

The whole stack runs on one free ARM VM (Ubuntu, Docker), with Caddy issuing
HTTPS certificates. The release is built on Windows and copied over SSH.

The full walkthrough is in **`deployment-oracle-guide.md`**. It covers:

- the account, the VM and both firewalls;
- the address (sslip.io or a domain);
- the deployment files to add (`application-prod.yml` and `deploy/oracle/`);
- shipping the release and writing the `.env`;
- Keycloak setup, catalogue import, tests, backups, day-to-day operation and
  troubleshooting.

---

## 5. Option C: Render + Neon + managed services

Each piece goes to a different free service; Keycloak and MinIO leave the
stack because they fit no free plan. This option changes the app the most, and
the result still sleeps after 15 idle minutes.

| Piece | Goes to | Relevant free limit |
| --- | --- | --- |
| API + web app | Render (Web Service, Docker) | Sleeps after 15 min, ~1 min to wake, 750 h/month, ephemeral disk |
| PostgreSQL | Neon | Compute suspends when idle and wakes on the next query; 100 compute-hours/month |
| Login (replaces Keycloak) | Managed OIDC provider with a free plan (e.g. Auth0) | Not verified in the research; check current limits |
| Portraits (replace MinIO) | Cloudflare R2 or another S3-compatible store | R2: 10 GB; sources disagree on whether a card is required |

Do not use Render's free Postgres: it **expires 30 days** after creation and is
deleted 14 days later.

What changes in the code:

- **Serve the web app from the API.** Render runs a single service, so there
  is no Caddy. Today only `DesktopWebAppConfig` (profile `desktop`) serves the
  web build, so a production equivalent is needed, plus a multi-stage
  Dockerfile that runs `npm run build` and `bootJar`.
- **Swap the login provider.** Point the `keycloak` registration at the new
  provider: issuer, endpoints, client id and secret, and
  `app.auth.end-session-uri`. Then review `KeycloakRealmRoles`, because realm
  roles are a Keycloak-specific claim. Existing players' `sub` values will not
  match the new provider.
- **Point storage at R2.** Set `storage.endpoint` and `public-endpoint` to
  `https://<account-id>.r2.cloudflarestorage.com`, with region `auto` and the
  R2 token's keys.
- **Database on Neon.** Use `jdbc:postgresql://<host>/<db>?sslmode=require`.
  Flyway creates the schema; the catalogue import must run once from a
  machine that can reach Neon.
- **Fit the 512 MB instance.** Set
  `JAVA_TOOL_OPTIONS=-Xmx300m -XX:+UseSerialGC -Xss512k` and
  `SPRING_MAIN_LAZY_INITIALIZATION=true`.

---

## 6. Sources

- [Render — Deploy for Free](https://render.com/docs/free)
- [Oracle — Always Free Resources](https://docs.oracle.com/en-us/iaas/Content/FreeTier/resourceref.htm)
- [InfoQ — Oracle Cloud free tier limits (2026)](https://infoq.com/news/2026/07/oracle-cloud-free-tier-limits/)
- [Cloudflare — Quick Tunnels](https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/)
- [Free developer tunnels comparison (2026)](https://merginit.com/blog/19062026-free-developer-tunnels-comparison)
- [Cloudflare Tunnel setup guide (2026)](https://tech-insider.org/cloudflare-tunnel-setup-2026/)
- [Neon vs Supabase free plan](https://neon.com/guides/neon-vs-supabase-free-plan)
- [Free PostgreSQL hosting (2026)](https://selfhost.dev/best-free-postgresql-hosting/)
- [Railway — Keycloak template requirements](https://railway.com/deploy/keycloak-iam)
- [selfhosting.sh — Keycloak](https://selfhosting.sh/apps/keycloak/)
- [Keycloak forum — memory limits](https://forum.keycloak.org/t/windows-docker-keycloak-memory-limits/8416)
- [Cloudflare R2 free tier](https://freetier.co/directory/products/cloudflare-r2)
- [Koyeb pricing](https://www.koyeb.com/pricing)
