package dev.omnisheetvault.api.identity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PlayerRepository extends JpaRepository<Player, UUID> {

    Optional<Player> findBySubject(String subject);

    List<Player> findByDisplayName(String displayName);

    /** Adds the tutorial to the player's seen list in one statement, so two concurrent marks can't lose one. */
    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE players SET seen_tutorials = array_append(seen_tutorials, :tutorial)
            WHERE id = :id AND NOT (:tutorial = ANY (seen_tutorials))""", nativeQuery = true)
    int markTutorialSeen(@Param("id") UUID id, @Param("tutorial") String tutorial);
}
