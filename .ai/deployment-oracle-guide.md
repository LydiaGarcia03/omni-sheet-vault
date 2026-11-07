# Deploying to Oracle Cloud Always Free — step-by-step guide

A guided walkthrough to put omni-sheet-vault online for free on an Oracle
Cloud VM, so friends open it from a link such as
`https://app.140.238.1.2.sslip.io` with no download.

Follow the phases in order. Each step says what to do, and **Check** says how
to know it worked before moving on. The comparison with the other hosting
options, and the reasons behind this layout, are in `deployment-options.md`.

Written on 2026-10-13 and not yet tried end to end. When a screen or a command
differs from what is written here, update this guide.

---

## Progress

Tick these as you go.

- [ ] Phase 0 — Gather what you need
- [ ] Phase 1 — Create the Oracle account
- [ ] Phase 2 — Create the VM
- [ ] Phase 3 — Open the ports
- [ ] Phase 4 — Install Docker on the VM
- [ ] Phase 5 — Choose the address
- [ ] Phase 6 — Add the deployment files to the repository (one time)
- [ ] Phase 7 — Build and send the release
- [ ] Phase 8 — Write the server's `.env`
- [ ] Phase 9 — Start and configure Keycloak
- [ ] Phase 10 — Start the API and the website
- [ ] Phase 11 — Import the catalogue
- [ ] Phase 12 — Test and share the link
- [ ] Phase 13 — Set up backups

---

## How it fits together

```
Friend's browser
      │  https
      ▼
┌──────────────────────── Oracle VM (Ubuntu, Docker) ────────────────────────┐
│  Caddy  (ports 80/443, automatic HTTPS certificates)                        │
│   ├─ app.<host>    → web app files  +  /api /oauth2 /login/oauth2 /logout → API │
│   ├─ auth.<host>   → Keycloak (login pages)                                  │
│   └─ files.<host>  → MinIO (portraits)                                       │
│                                                                              │
│  API (Spring Boot, prod profile) ── PostgreSQL ── Keycloak ── MinIO          │
│  (internal Docker network only; nothing else is reachable from outside)     │
└──────────────────────────────────────────────────────────────────────────────┘
      ▲
      │  scp (release package)  ·  ssh (admin)
Your Windows machine: builds the jar and the web app, keeps the code
```

Nothing is built on the VM. Your Windows machine builds the jar and the web
app, packs them with `infra/` and `content/`, and copies the package over SSH.
No Git push is involved, so `content/` stays out of the public remote
(adr-0010).

---

## Phase 0 — Gather what you need

- A **credit or debit card**. Oracle uses it only to verify identity. Always
  Free resources are not charged.
- A **phone** for the sign-up verification.
- **Windows PowerShell** with OpenSSH. It is built into Windows 10; check
  with `ssh -V`.
- This repository building locally: `.\gradlew.bat :apps:api:bootJar` and
  `npm run build` in `apps\web` both succeed.
- A **password manager** or a safe note for the new production passwords.
- About two to three hours for the first deployment.

**Check:** `ssh -V` prints a version, and both builds pass.

---

## Phase 1 — Create the Oracle account

1. Go to **oracle.com/cloud/free** and choose **Start for free**.
2. Fill in the form, then verify your e-mail and your phone.
3. **Home region** — pick it carefully, because it cannot be changed later.
   **Brazil East (São Paulo)** or **Brazil Southeast (Vinhedo)** give the
   lowest latency for players in Brazil.
4. Enter the card. A small authorisation may appear and is reversed.
5. Wait for the e-mail saying the account is ready (minutes, sometimes
   hours), then sign in at **cloud.oracle.com**.

**Check:** the Oracle Cloud console opens and shows your home region in the
top bar.

---

## Phase 2 — Create the VM

1. In the console: **☰ → Compute → Instances → Create instance**.
2. **Name:** `omni-sheet-vault`.
3. **Image and shape** (in the wizard's first step, *Basic information*):
   1. **Change image** → *Canonical Ubuntu* → version **24.04** → **Select image**.
   2. **Change shape** → *Virtual machine* → *Ampere* → tick
      **VM.Standard.A1.Flex**.
   3. Click the small **▸ arrow** left of the shape name to expand its row.
      Only then do *Number of OCPUs* and *Amount of memory (GB)* appear.
   4. Set **OCPUs = 2** and **Memory = 12 GB**, or the maximum that keeps the
      *Always Free-eligible* label.
   5. **Select shape**. The summary now reads e.g. *2 core OCPU, 12 GB
      memory*.

   The line *1 core OCPU, 6 GB memory, 1 Gbps network bandwidth* is only the
   summary of A1.Flex's default size. It changes through **Change shape**,
   never in place.

   If the fields do not go past 1 OCPU, check the account's limit:

   - Open **☰ → Governance & Administration → Limits, Quotas and Usage**.
   - Filter *Service: Compute*, then find *Standard.A1 core count*.
   - If the limit is 1, create the VM with **1 OCPU / 6 GB**. The app runs,
     with three adjustments marked **1 OCPU** below (Phase 4 and Phase 6.2).
   - The shape can be raised later without recreating anything
     (**Day to day → Resize the VM**).
4. **Security** step: keep the defaults.
5. **Networking** step. Before this step, create the network on its own. The
   wizard's *Create new public subnet* leaves *Subnet IPv4 prefixes* empty
   and keeps the public IPv4 toggle disabled ("You must select a public
   subnet…"):
   1. In a new tab: **☰ → Networking → Virtual cloud networks** → compartment
      *(root)* → **Actions → Start VCN Wizard** → **Create VCN with Internet
      Connectivity**.
   2. Name it `omni-vcn`, keep the rest, then **Next** → **Create**.
   3. Back in the instance wizard (restart it if the new VCN is not listed),
      fill the step as follows.
   - *Primary VNIC information*: the name is optional.
   - *Primary network*: **Select existing virtual cloud network** →
     `omni-vcn`.
   - *Subnet*: **Select existing subnet** → **public subnet-omni-vcn** (not
     the private one).
   - *Subnet IPv4 prefixes* now shows `10.0.0.0/24`. Private IPv4:
     **Automatically assign**. **Automatically assign public IPv4 address:
     on**; without it the VM has no `<ip>`. Leave IPv6 off.
   - If the toggle still refuses, create the instance without it. Then add
     the IP: **Instance → Attached VNICs → primary VNIC → IPv4 Addresses →
     ⋮ → Edit → Ephemeral public IP → Update**.
6. **Add SSH keys** (same step): choose **Generate a key pair for me**, then
   **Download private key** *before* creating the instance; it cannot be
   downloaded again. Save it as `C:\Users\<you>\.ssh\osv-oracle.key`. This
   guide calls it `<key>`. The public key is not needed.
7. **Storage** step: keep the default boot volume.
8. **Review** → **Create**.
   - If you see **Out of capacity**, go back and pick another *Availability
     domain* (AD-1, AD-2, …) in the *Placement* section.
   - If every availability domain fails, try again a few hours later. Free
     ARM machines are in high demand.
8. Wait until the state turns **Running**, then copy the **Public IP
   address** from the instance page. This guide calls it `<ip>`.

**Check:** the instance shows *Running* and a public IP.

> Note: third-party sources report that Oracle cut the free ARM allowance to
> 2 OCPUs / 12 GB in June 2026, and that Oracle may reclaim Always Free VMs
> that stay almost idle for long periods. Phase 13 makes sure a reclaimed VM
> loses nothing.

---

## Phase 3 — Open the ports

The VM has two firewalls, and both must let web traffic in.

### 3.1 Oracle's firewall (Security List)

1. On the instance page, under **Primary VNIC**, click the **Subnet** link.
2. Open the **Security** tab (or **Security Lists**) and click **Default
   Security List for …**.
3. **Add Ingress Rules**:
   - *Source CIDR* `0.0.0.0/0`, *IP Protocol* **TCP**, *Destination Port
     Range* **80**.
   - **+ Another Ingress Rule**: the same with port **443**.
4. **Add Ingress Rules** to save. Port 22 (SSH) is already open by default.

### 3.2 Connect to the VM for the first time

In PowerShell on Windows:

```powershell
icacls "$HOME\.ssh\osv-oracle.key" /inheritance:r /grant:r "$($env:USERNAME):R"
ssh -i "$HOME\.ssh\osv-oracle.key" ubuntu@<ip>
```

- The `icacls` line runs once: `ssh` refuses a key that other users can read.
- Answer **yes** to the fingerprint question.

**Check:** the prompt changes to `ubuntu@omni-sheet-vault:~$`.

### 3.3 The VM's own firewall

Still connected to the VM:

```bash
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save
```

**Check:** `sudo iptables -L INPUT -n --line-numbers` lists two `ACCEPT`
rules for `dpt:80` and `dpt:443`.

---

## Phase 4 — Install Docker on the VM

```bash
sudo apt update && sudo apt -y upgrade
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
exit
```

Reconnect with the same `ssh` command, then:

```bash
docker run --rm hello-world
```

Add a 4 GB swap file as a safety margin for the two JVMs. It is optional with
12 GB of memory, and **required on a 1 OCPU / 6 GB VM**:

```bash
sudo fallocate -l 4G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

**Check:** `hello-world` prints *Hello from Docker!*, and `docker compose
version` prints a version.

---

## Phase 5 — Choose the address

The app needs three names that point at `<ip>`. This guide calls the shared
part `<host>`.

**Option 1 — sslip.io (free, nothing to set up).** sslip.io answers any name
that contains an IP with that IP. With `<ip>` = `140.238.1.2`:

| Name | Value |
| --- | --- |
| `<host>` | `140.238.1.2.sslip.io` |
| App (the link you share) | `app.140.238.1.2.sslip.io` |
| Login | `auth.140.238.1.2.sslip.io` |
| Portraits | `files.140.238.1.2.sslip.io` |

**Option 2 — your own domain.** At your DNS provider, create three `A`
records, `app`, `auth` and `files`, all pointing at `<ip>`. Then `<host>` is
your domain.

**Check:** on Windows, `nslookup app.<host>` answers `<ip>`. Do the same for
`auth.<host>` and `files.<host>`.

The IP stays the same while the instance exists. If the instance is ever
recreated, the IP changes: redo this phase, then update `.env` (Phase 8) and
Keycloak (Phase 9).

---

## Phase 6 — Add the deployment files to the repository (one time)

These five files do not exist yet. Create them once; every later release
reuses them. Claude can create them on request.

When they exist, update `CLAUDE.md` section 4 and `architecture.md` to list
`deploy/`, and add an entry to `changelog.md`.

### 6.1 `apps/api/src/main/resources/application-prod.yml`

Overrides only what differs from `application.yml`. The browser gets the
public Keycloak address. The API talks to Keycloak over Docker's internal
network.

```yaml
# Public deployment behind Caddy (see .ai/deployment-oracle-guide.md).
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://${AUTH_HOST}/realms/omni-sheet-vault
          jwk-set-uri: http://keycloak:8081/realms/omni-sheet-vault/protocol/openid-connect/certs
      client:
        provider:
          keycloak:
            authorization-uri: https://${AUTH_HOST}/realms/omni-sheet-vault/protocol/openid-connect/auth
            token-uri: http://keycloak:8081/realms/omni-sheet-vault/protocol/openid-connect/token
            jwk-set-uri: http://keycloak:8081/realms/omni-sheet-vault/protocol/openid-connect/certs
            user-info-uri: http://keycloak:8081/realms/omni-sheet-vault/protocol/openid-connect/userinfo

app:
  auth:
    end-session-uri: https://${AUTH_HOST}/realms/omni-sheet-vault/protocol/openid-connect/logout

storage:
  endpoint: http://minio:9000
  public-endpoint: https://${FILES_HOST}

server:
  # Caddy sends X-Forwarded-Proto/Host, so the login redirect URI is built as https://app.<host>.
  forward-headers-strategy: framework
```

### 6.2 `deploy/oracle/docker-compose.yml`

```yaml
name: omni-sheet-vault

services:
  postgres:
    image: postgres:17-alpine
    restart: unless-stopped
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./infra/postgres/init:/docker-entrypoint-initdb.d:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5

  keycloak:
    image: quay.io/keycloak/keycloak:26.4
    restart: unless-stopped
    command: ["start", "--import-realm"]
    mem_limit: 2g
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: ${KEYCLOAK_ADMIN_USER}
      KC_BOOTSTRAP_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: ${POSTGRES_USER}
      KC_DB_PASSWORD: ${POSTGRES_PASSWORD}
      KC_HEALTH_ENABLED: "true"
      KC_HTTP_ENABLED: "true"
      KC_HTTP_PORT: 8081
      KC_HOSTNAME: https://${AUTH_HOST}
      KC_HOSTNAME_ADMIN: http://localhost:8081
      KC_HOSTNAME_BACKCHANNEL_DYNAMIC: "true"
      KC_PROXY_HEADERS: xforwarded
    ports:
      - "127.0.0.1:8081:8081"
    volumes:
      - ./infra/keycloak:/opt/keycloak/data/import:ro
      - ./infra/keycloak-themes/omni-sheet-vault:/opt/keycloak/themes/omni-sheet-vault:ro
    depends_on:
      postgres:
        condition: service_healthy

  minio:
    image: minio/minio:<the tag used locally, pinned>
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ROOT_USER}
      MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD}
    ports:
      - "127.0.0.1:9001:9001"
    volumes:
      - minio-data:/data
    healthcheck:
      test: ["CMD", "mc", "ready", "local"]
      interval: 15s
      timeout: 5s
      retries: 5

  api:
    image: eclipse-temurin:25-jre
    restart: unless-stopped
    entrypoint: ["java", "-Xmx1g", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
    env_file: .env
    volumes:
      - ./api/app.jar:/app/app.jar:ro
      - ./content:/content:ro
    depends_on:
      postgres:
        condition: service_healthy
      minio:
        condition: service_healthy
      keycloak:
        condition: service_started

  caddy:
    image: caddy:2
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    env_file: .env
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile:ro
      - ./web:/srv/web:ro
      - caddy-data:/data
      - caddy-config:/config
    depends_on:
      - api
      - keycloak
      - minio

volumes:
  postgres-data:
  minio-data:
  caddy-data:
  caddy-config:
```

**1 OCPU / 6 GB VM:**

- Change Keycloak's `mem_limit: 2g` to `mem_limit: 1536m`, and the API's
  `-Xmx1g` to `-Xmx768m`.
- The whole stack then uses about 3.5–4 GB:

  | Service | Memory |
  | --- | --- |
  | Keycloak | ~1.5 GB |
  | API | ~1.2 GB |
  | Postgres + MinIO + Caddy | ~0.5 GB |

- Keycloak and the API each take one to two minutes to start on a single
  core. After startup, a handful of players see normal speed.
- After a resize to 2 OCPU / 12 GB, restore the original values.

Only Caddy is reachable from outside. The Keycloak admin console (8081) and
the MinIO console (9001) listen on the VM's `127.0.0.1` only, and are opened
through an SSH tunnel.

### 6.3 `deploy/oracle/Caddyfile`

```
{$APP_HOST} {
    encode zstd gzip

    @backend path /api/* /oauth2/* /login/oauth2/* /logout
    handle @backend {
        reverse_proxy api:8090
    }

    handle {
        root * /srv/web
        header /assets/* Cache-Control "public, max-age=31536000, immutable"
        try_files {path} /index.html
        file_server
    }
}

{$AUTH_HOST} {
    @admin path /admin /admin/*
    respond @admin 403
    reverse_proxy keycloak:8081
}

{$FILES_HOST} {
    reverse_proxy minio:9000
}
```

This is the production version of Vite's dev proxy. The same four paths go to
the API, and every other path is the web app.

### 6.4 `deploy/oracle/.env.example`

```
APP_HOST=
AUTH_HOST=
FILES_HOST=

POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_DB=omni_sheet_vault

KEYCLOAK_ADMIN_USER=
KEYCLOAK_ADMIN_PASSWORD=
# Secret of the confidential "omni-sheet-vault-bff" client, copied from Keycloak after the first start.
KEYCLOAK_BFF_CLIENT_SECRET=

MINIO_ROOT_USER=
MINIO_ROOT_PASSWORD=
```

### 6.5 `deploy/oracle/package-release.ps1`

```powershell
# Builds the API jar and the web app, and gathers what the server needs into build/release.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path "$PSScriptRoot\..\..").Path
$out = Join-Path $root 'build\release'

Push-Location $root
try {
    .\gradlew.bat :apps:api:bootJar
    Push-Location apps\web; npm ci; npm run build; Pop-Location

    if (Test-Path $out) { Remove-Item -Recurse -Force $out }
    New-Item -ItemType Directory -Force "$out\api" | Out-Null
    Get-ChildItem apps\api\build\libs\*.jar | Where-Object Name -notlike '*-plain.jar' |
        Copy-Item -Destination "$out\api\app.jar"
    Copy-Item -Recurse apps\web\dist "$out\web"
    Copy-Item -Recurse infra "$out\infra"
    Copy-Item -Recurse content "$out\content"
    Copy-Item deploy\oracle\docker-compose.yml, deploy\oracle\Caddyfile -Destination $out

    tar -czf (Join-Path $root 'build\osv-release.tgz') -C $out .
} finally {
    Pop-Location
}
```

**Check:** `.\gradlew.bat :apps:api:check` is still green. The new profile is
only read when `prod` is active.

---

## Phase 7 — Build and send the release

On Windows, from the repository root:

```powershell
.\deploy\oracle\package-release.ps1
scp -i "$HOME\.ssh\osv-oracle.key" build\osv-release.tgz ubuntu@<ip>:~
```

On the VM:

```bash
mkdir -p ~/omni && tar -xzf ~/osv-release.tgz -C ~/omni && cd ~/omni && ls
```

**Check:** `ls` shows `api  Caddyfile  content  docker-compose.yml  infra  web`.

---

## Phase 8 — Write the server's `.env`

Still in `~/omni` on the VM:

```bash
touch .env && chmod 600 .env && nano .env
```

Paste the contents of `.env.example` and fill them in:

- `APP_HOST`, `AUTH_HOST`, `FILES_HOST` — the three names from Phase 5, with
  no `https://` (e.g. `app.140.238.1.2.sslip.io`).
- Every password **new**, different from your local `.env`. Generate each one
  on the VM with `openssl rand -base64 24`.
- Leave `KEYCLOAK_BFF_CLIENT_SECRET` empty for now; Phase 9 fills it.

Save with `Ctrl+O`, `Enter`, then exit with `Ctrl+X`. Copy the same values
into your password manager.

**Check:** `docker compose config --quiet` prints nothing, which means the
compose file and `.env` are valid.

---

## Phase 9 — Start and configure Keycloak

### 9.1 Start the infrastructure

```bash
docker compose up -d postgres keycloak minio
docker compose logs -f keycloak
```

Wait for a line containing `Listening on: http://0.0.0.0:8081`, then press
`Ctrl+C`. That stops only the log view; the container keeps running.

On this first start:

- `infra/postgres/init` creates the `keycloak` database.
- `--import-realm` creates the `omni-sheet-vault` realm with its login theme.

### 9.2 Open the admin console through an SSH tunnel

Open a **new** PowerShell window on Windows and leave it open:

```powershell
ssh -i "$HOME\.ssh\osv-oracle.key" -L 8081:localhost:8081 -L 9001:localhost:9001 ubuntu@<ip>
```

Browse to **http://localhost:8081/admin** and log in with
`KEYCLOAK_ADMIN_USER` / `KEYCLOAK_ADMIN_PASSWORD` from the VM's `.env`.

### 9.3 Point the app's client at the public address

1. In the realm selector (top left), switch to **omni-sheet-vault**.
2. **Clients → omni-sheet-vault-bff → Settings**:
   - **Valid redirect URIs**: add
     `https://app.<host>/login/oauth2/code/keycloak`, then delete the two
     `localhost` entries.
   - **Valid post logout redirect URIs**: replace the value with
     `https://app.<host>/*`.
   - **Save**.
3. **Credentials** tab → **Regenerate** next to *Client secret* → copy the
   value.
4. On the VM, put it in `.env` as `KEYCLOAK_BFF_CLIENT_SECRET=<value>`.

### 9.4 Decide who can sign up

- **Realm settings → Login → User registration.** The realm export ships it
  **on**, so anyone with the link can create an account.
- To invite friends only, turn it **off** and create each account under
  **Users → Add user**. Then set a temporary password in **Credentials**, with
  *Temporary* on, so they choose their own on first login.
- *Forgot password* needs an e-mail server (**Realm settings → Email**).
  Without one, leave it off and reset passwords by hand under **Users**.

**Check:** **Clients → omni-sheet-vault-bff** shows the new redirect URI, and
`KEYCLOAK_BFF_CLIENT_SECRET` is filled in the VM's `.env`.

---

## Phase 10 — Start the API and the website

On the VM, in `~/omni`:

```bash
docker compose up -d
docker compose logs -f caddy
```

Wait for `certificate obtained successfully` for each of the three names,
then press `Ctrl+C`.

```bash
docker compose logs -f api
```

Wait for `Started OmniSheetVaultApplication in … seconds`, then press
`Ctrl+C`. On this first start, Flyway creates every table first.

**Check:**

- `docker compose ps` lists six services, all `running` (or `healthy`).
- `https://app.<host>` opens the app's home page in your browser, with a
  padlock icon.

---

## Phase 11 — Import the catalogue

The server's database starts with an empty catalogue: no spells, classes or
cards. Import it with the same `--import-catalogue` command as
`developer-guide.md` 1.3. Each kind runs in a one-off container that exits
when done.

```bash
cd ~/omni
for kind in backgrounds classes conditions creatures feats items languages optional_features species spells subclasses; do
  docker compose run --rm api --import-catalogue=/content/dnd-5e/$kind
done
for kind in classes subclasses domains domain_cards ancestries communities transformations items; do
  docker compose run --rm api --import-catalogue=/content/daggerheart/$kind
done
```

- `builds/` and `mechanics/` are skipped on purpose: they are not catalogue
  kinds.
- When `content/` gains a new kind, add it to these lists.
- The public deployment keeps catalogue redaction on (adr-0005), as in
  `application.yml`.

**Check:** creating a D&D character in the browser offers classes, species
and spells. Creating a Daggerheart character offers classes and ancestries.

---

## Phase 12 — Test and share the link

Use a phone on **mobile data**, not your home Wi-Fi, and open
`https://app.<host>`:

1. The login page shows the omni-sheet-vault theme, at `auth.<host>`.
2. Register (or log in) and land back on `app.<host>`.
3. Create one character in each system.
4. Upload a portrait; it appears, served from `files.<host>`.
5. Roll dice; the roll appears in the game log.
6. Reload the page while on a character sheet; the sheet opens again.
7. Log out, and land back on the app, logged out.

Then run the safety checks:

- `https://auth.<host>/admin` answers **403**.
- From Windows, `Test-NetConnection <ip> -Port 5432` **fails**. Repeat for
  ports 8081, 8090, 9000 and 9001.
- Licensing: confirm that publishing each system's content is allowed
  (adr-0005, adr-0010, and the pending VtM licence check) before the link
  leaves a small circle of friends.

**Check:** every item passes. Share `https://app.<host>`.

---

## Phase 13 — Set up backups

### 13.1 Daily database backup, kept 14 days

On the VM:

```bash
mkdir -p ~/backups
crontab -e
```

Choose `nano` if asked, and add this line at the end:

```
0 4 * * * cd /home/ubuntu/omni && docker compose exec -T postgres pg_dumpall -U $(grep ^POSTGRES_USER .env | cut -d= -f2) | gzip > /home/ubuntu/backups/db-$(date +\%F).sql.gz && find /home/ubuntu/backups -name 'db-*' -mtime +14 -delete
```

### 13.2 Portraits

The portraits live in the MinIO volume. Back it up by hand from time to time:

```bash
docker run --rm -v omni-sheet-vault_minio-data:/d -v ~/backups:/b alpine tar czf /b/minio-$(date +%F).tgz -C /d .
```

### 13.3 Copy the backups off the VM

From Windows, now and then:

```powershell
New-Item -ItemType Directory -Force .\osv-backups | Out-Null
scp -i "$HOME\.ssh\osv-oracle.key" "ubuntu@<ip>:~/backups/*" .\osv-backups\
```

**Check:** the next morning, `ls ~/backups` shows a `db-<date>.sql.gz` file.

---

## Day to day

| Task | How |
| --- | --- |
| **Ship a new version** | Windows: `.\deploy\oracle\package-release.ps1`, then `scp` (Phase 7). VM: `cd ~/omni && tar -xzf ~/osv-release.tgz -C ~/omni && docker compose up -d && docker compose restart api` |
| New Flyway migration | Nothing extra: applied when the API restarts |
| Catalogue content changed | Re-run the import for that kind (Phase 11) |
| See logs | `docker compose logs --tail=200 api` (or `keycloak`, `caddy`) |
| Look inside the database | `docker compose exec postgres psql -U <POSTGRES_USER> -d omni_sheet_vault`, then `\dt` lists tables, `SELECT * FROM <table> LIMIT 10;` reads rows, `\q` leaves |
| Browse portraits | SSH tunnel from 9.2 open, then `http://localhost:9001` with the MinIO user from `.env` |
| Keycloak admin | SSH tunnel from 9.2 open, then `http://localhost:8081/admin` |
| Stop everything | `docker compose down`. It keeps all data. **Never** add `-v`, which erases the volumes |
| Start everything | `docker compose up -d` |
| Resize the VM | Oracle console: **Instance → More actions → Edit → Edit shape**, then raise OCPUs/memory within the free limit. The VM reboots and keeps its disk and IP, and `restart: unless-stopped` brings the stack back. Then revert the 1 OCPU values from Phase 6.2 |
| Restore a database backup | `gunzip -c ~/backups/db-<date>.sql.gz \| docker compose exec -T postgres psql -U <POSTGRES_USER> -d postgres` on an empty `postgres-data` volume |

`.env` and the Docker volumes are not part of the release, so a new version
never touches them.

---

## When something goes wrong

| Symptom | Likely cause and fix |
| --- | --- |
| *Out of capacity* when creating the VM | No free ARM capacity in that availability domain. Try another one, or retry later (Phase 2) |
| `ssh` says *Permissions … are too open* | Run the `icacls` line from 3.2 again |
| Caddy logs `challenge failed`, no padlock | Port 80/443 closed in the Security List or in `iptables` (Phase 3), or `nslookup` does not answer `<ip>` yet (Phase 5) |
| Keycloak shows *Invalid parameter: redirect_uri* | The redirect URI from 9.3 is missing or has a typo |
| Login sends you to `http://…` or to `localhost` | `forward-headers-strategy` missing in `application-prod.yml`, or `APP_HOST` / `AUTH_HOST` empty in `.env` |
| API logs `invalid_client` after login | `KEYCLOAK_BFF_CLIENT_SECRET` differs from Keycloak's *Credentials* tab. Fix `.env`, then `docker compose up -d api` |
| API logs `The iss claim is not valid` | The `issuer-uri` in `application-prod.yml` differs from `KC_HOSTNAME`; both must be `https://auth.<host>` |
| Portraits do not load (*SignatureDoesNotMatch*) | `FILES_HOST` differs from the name the browser uses |
| Logged out on every click | The page was opened over `http://`; the session cookie needs `https://` |
| The app is empty when creating a character | The catalogue was not imported (Phase 11) |
| A container keeps restarting | `docker compose logs --tail=100 <service>` shows why; usually a wrong value in `.env` |
| The VM no longer answers at all | Check its state in the Oracle console. If it was reclaimed, create a new one and redo the phases, restoring the latest backup |
