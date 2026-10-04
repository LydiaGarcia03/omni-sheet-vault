package dev.omnisheetvault.api.identity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PlayerRepository extends JpaRepository<Player, UUID> {

    Optional<Player> findBySubject(String subject);

    List<Player> findByDisplayName(String displayName);
}
