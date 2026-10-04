package dev.omnisheetvault.api.identity;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Bridges an access token to its local {@link Player} row, creating one on first
 * sight of a subject. Any endpoint needing the authenticated player goes through
 * this, not through the repository directly.
 */
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate requiresNewTransaction;

    public PlayerService(
            PlayerRepository playerRepository,
            ApplicationEventPublisher eventPublisher,
            PlatformTransactionManager transactionManager) {
        this.playerRepository = playerRepository;
        this.eventPublisher = eventPublisher;
        this.requiresNewTransaction = new TransactionTemplate(
                transactionManager, new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
    }

    public Player currentPlayer(Jwt jwt) {
        return playerRepository.findBySubject(jwt.getSubject()).orElseGet(() -> createOrFindPlayer(jwt));
    }

    /** Players by their Keycloak username, for command-line tools that act on a named local account. */
    public List<Player> playersNamed(String displayName) {
        return playerRepository.findByDisplayName(displayName);
    }

    /**
     * Two requests can both pass {@code currentPlayer}'s own lookup for a subject
     * neither has seen before, then both attempt to create it — found live 2026-09-17
     * (a manual local-dev row deletion, re-triggering "first sight", raced two of the
     * frontend's own concurrent requests). The losing insert violates {@code
     * players_subject_key}. Run in its own transaction (not the caller's ambient one):
     * the actual insert — and so the constraint check — is deferred to commit, which
     * for the caller's own surrounding {@code @Transactional} method happens well
     * after this method already returned, outside any local try/catch here. Forcing
     * the commit inside this method's own call frame, via {@link TransactionTemplate},
     * is what makes the violation catchable at all; a losing attempt then simply reads
     * back the winner's row instead of failing the whole request.
     */
    private Player createOrFindPlayer(Jwt jwt) {
        try {
            return requiresNewTransaction.execute(status -> {
                Player player = playerRepository.save(Player.create(jwt.getSubject(), displayNameFrom(jwt)));
                eventPublisher.publishEvent(new PlayerCreatedEvent(player.id()));
                return player;
            });
        } catch (DataIntegrityViolationException lostTheRace) {
            return playerRepository.findBySubject(jwt.getSubject()).orElseThrow(() -> lostTheRace);
        }
    }

    private String displayNameFrom(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        return preferredUsername != null ? preferredUsername : jwt.getSubject();
    }
}
