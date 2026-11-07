package dev.omnisheetvault.api.identity;

import java.util.List;
import java.util.UUID;

/**
 * The authenticated player. {@code displayName} is the name shown in the app, {@code username} the Keycloak login name;
 * {@code avatarUrl} is a short-lived link to the profile picture, or null without one.
 */
public record CurrentUserResponse(
        UUID id, String subject, String displayName, String username, String email, List<String> roles, PreferencesResponse preferences,
        String avatarUrl) {

    /** The player's chosen interface language and theme; null where they haven't chosen. */
    public record PreferencesResponse(String locale, String theme) {

        static PreferencesResponse of(Player player) {
            return new PreferencesResponse(player.preferredLocale(), player.preferredTheme());
        }
    }
}
