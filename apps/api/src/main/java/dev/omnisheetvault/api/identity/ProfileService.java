package dev.omnisheetvault.api.identity;

import dev.omnisheetvault.api.shared.KeycloakRealmRoles;
import dev.omnisheetvault.api.storage.PortraitImage;
import dev.omnisheetvault.api.storage.PortraitStorage;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * The player's own profile: the name shown in the app and the profile picture. Username, email and
 * password belong to Keycloak and are changed on its screens, never here.
 */
@Service
public class ProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);

    private final PlayerService playerService;
    private final PortraitStorage storage;

    ProfileService(PlayerService playerService, PortraitStorage storage) {
        this.playerService = playerService;
        this.storage = storage;
    }

    public CurrentUserResponse currentUser(Jwt jwt) {
        return describe(jwt, playerService.currentPlayer(jwt));
    }

    public CurrentUserResponse rename(Jwt jwt, String name) {
        return describe(jwt, playerService.rename(jwt, name));
    }

    public CurrentUserResponse uploadAvatar(Jwt jwt, byte[] file) {
        Player player = playerService.currentPlayer(jwt);
        byte[] picture = PortraitImage.normalize(file);
        String key = "avatars/" + player.id() + "/" + UUID.randomUUID() + ".png";
        storage.store(key, picture, PortraitImage.CONTENT_TYPE);
        return describe(jwt, replaceAvatar(player, key));
    }

    public CurrentUserResponse removeAvatar(Jwt jwt) {
        return describe(jwt, replaceAvatar(playerService.currentPlayer(jwt), null));
    }

    private Player replaceAvatar(Player player, String key) {
        String previous = player.avatarKey();
        Player saved = playerService.changeAvatar(player, key);
        if (previous != null) {
            deleteQuietly(previous);
        }
        return saved;
    }

    /** Identity from the local row; email and roles read live from the token, since Keycloak owns them. */
    private CurrentUserResponse describe(Jwt jwt, Player player) {
        return new CurrentUserResponse(
                player.id(),
                player.subject(),
                player.shownName(),
                player.displayName(),
                jwt.getClaimAsString("email"),
                KeycloakRealmRoles.from(jwt),
                CurrentUserResponse.PreferencesResponse.of(player),
                player.avatarKey() == null ? null : storage.temporaryUrl(player.avatarKey()).toString());
    }

    /** A leftover object costs storage, not correctness, so a failed cleanup never fails the change. */
    private void deleteQuietly(String key) {
        try {
            storage.delete(key);
        } catch (RuntimeException e) {
            LOG.warn("Could not delete replaced profile picture {}", key, e);
        }
    }
}
