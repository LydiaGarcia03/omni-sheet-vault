package dev.omnisheetvault.api.character;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

interface CharacterRepository extends JpaRepository<Character, UUID> {

    List<Character> findByPlayerIdAndDeletedAtIsNull(UUID playerId);

    Optional<Character> findByIdAndPlayerIdAndDeletedAtIsNull(UUID id, UUID playerId);

    /** The only write to {@code portrait_key}, so a concurrent draft autosave can't put back a stale value. */
    @Transactional
    @Modifying
    @Query("UPDATE Character c SET c.portraitKey = :portraitKey, c.updatedAt = :now WHERE c.id = :id")
    void updatePortraitKey(UUID id, String portraitKey, Instant now);
}
