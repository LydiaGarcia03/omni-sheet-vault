# My profile

The player's own profile page, at `/profile`, reached from "My profile" in the
user menu. It replaces the link to Keycloak's account console, which players
should not reach: the console is disabled in the realm and its roles are no
longer granted by default.

## What the page holds

| Part | Owned by | How it changes |
| --- | --- | --- |
| Profile picture | The app (`players.avatar_key`, portrait storage) | Upload (PNG/JPEG, up to 3 MB, re-encoded as a square PNG of at most 512 px) or remove |
| Display name | The app (`players.profile_name`) | Form field, at most 40 characters; empty goes back to the username |
| Username | Keycloak | Read-only |
| Email | Keycloak | "Change email" opens Keycloak's own screen |
| Password | Keycloak | "Change password" opens Keycloak's own screen |

The app never handles a password or writes an email. The two buttons send the
browser to `/oauth2/authorization/keycloak?returnTo=/profile&action=password|email`;
`BrowserLoginRequestResolver` turns `action` into Keycloak's
application-initiated action (`kc_action=UPDATE_PASSWORD|UPDATE_EMAIL`), and the
login comes back to the profile with a fresh session. Unknown actions are
ignored. Keycloak may ask for the current password first. There is no SMTP, so
an email change is not verified by mail.

The desktop edition has no login, so the page there shows only the picture and
the display name.

## API

- `GET /api/me`: `displayName` (shown name), `username`, `email`, `roles`,
  `preferences`, `avatarUrl` (short-lived link, or null).
- `PUT /api/me/profile` `{displayName}`: returns the same shape.
- `POST /api/me/avatar` (multipart `file`) and `DELETE /api/me/avatar`: return
  the same shape. A replaced or removed picture's object is deleted; a failed
  delete is only logged.

Profile pictures live in the portrait storage under
`avatars/<player id>/<uuid>.png` (MinIO on the server edition, the data folder
on desktop, served by `LocalPortraitFileController`).

## Where it lives

- API: `identity/ProfileService`, `identity/CurrentUserController`,
  `storage/PortraitImage`, `shared/BrowserLoginRequestResolver`.
- Web: `profile/ProfilePage.tsx`, `profile/api.ts`, `auth/session.ts`
  (`redirectToAccountAction`), `shell/AppHeader.tsx` (menu link and picture),
  `locales/profile.{en,pt-BR}.json`.
