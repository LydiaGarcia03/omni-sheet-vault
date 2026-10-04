package dev.omnisheetvault.api.dice;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface RollRepository extends JpaRepository<Roll, UUID> {

    List<Roll> findByCharacterIdOrderByRolledAtDesc(UUID characterId);
}
