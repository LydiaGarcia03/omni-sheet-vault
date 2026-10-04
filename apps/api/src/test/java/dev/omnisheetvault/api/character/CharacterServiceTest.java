package dev.omnisheetvault.api.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
class CharacterServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private CharacterService characterService;

    @Test
    void listsOnlyTheCallersOwnCharacters() {
        Jwt owner = jwtFor("55555555-5555-5555-5555-555555555555");
        Jwt otherPlayer = jwtFor("66666666-6666-6666-6666-666666666666");

        characterService.create(owner, "Aria", "dnd-5e");
        characterService.create(otherPlayer, "Borin", "dnd-5e");

        assertThat(characterService.listMine(owner)).extracting(Character::name).containsExactly("Aria");
        assertThat(characterService.listMine(otherPlayer)).extracting(Character::name).containsExactly("Borin");
    }

    @Test
    void excludesSoftDeletedCharactersFromTheList() {
        Jwt owner = jwtFor("77777777-7777-7777-7777-777777777777");
        Character character = characterService.create(owner, "Cael", "dnd-5e");

        characterService.deleteMine(owner, character.id());

        assertThat(characterService.listMine(owner)).isEmpty();
    }

    @Test
    void readingAnotherPlayersCharacterIsNotFound() {
        Jwt owner = jwtFor("88888888-8888-8888-8888-888888888888");
        Jwt otherPlayer = jwtFor("99999999-9999-9999-9999-999999999999");
        Character character = characterService.create(owner, "Dara", "dnd-5e");

        assertThatThrownBy(() -> characterService.getMine(otherPlayer, character.id()))
                .isInstanceOf(CharacterNotFoundException.class);
    }

    @Test
    void deletingAnotherPlayersCharacterIsNotFound() {
        Jwt owner = jwtFor("11111111-2222-3333-4444-555555555555");
        Jwt otherPlayer = jwtFor("22222222-3333-4444-5555-666666666666");
        Character character = characterService.create(owner, "Eryn", "dnd-5e");

        assertThatThrownBy(() -> characterService.deleteMine(otherPlayer, character.id()))
                .isInstanceOf(CharacterNotFoundException.class);
    }

    @Test
    void renamesTheCallersCharacterTrimmingTheName() {
        Jwt owner = jwtFor("44444444-5555-6666-7777-888888888888");
        Character character = characterService.create(owner, "Gale", "dnd-5e");

        characterService.rename(owner, character.id(), "  Gale the Wise ");

        assertThat(characterService.getMine(owner, character.id()).name()).isEqualTo("Gale the Wise");
    }

    @Test
    void renamingAnotherPlayersCharacterIsNotFound() {
        Jwt owner = jwtFor("12121212-3434-5656-7878-909090909090");
        Jwt otherPlayer = jwtFor("21212121-4343-6565-8787-090909090909");
        Character character = characterService.create(owner, "Hale", "dnd-5e");

        assertThatThrownBy(() -> characterService.rename(otherPlayer, character.id(), "Stolen"))
                .isInstanceOf(CharacterNotFoundException.class);
    }

    @Test
    void creatingWithAnUnknownSystemIdIsRejected() {
        Jwt owner = jwtFor("33333333-4444-5555-6666-777777777777");

        assertThatThrownBy(() -> characterService.create(owner, "Fenn", "not-a-real-system"))
                .isInstanceOf(UnsupportedGameSystemException.class);
    }

    private Jwt jwtFor(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("preferred_username", "player-" + subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }
}
