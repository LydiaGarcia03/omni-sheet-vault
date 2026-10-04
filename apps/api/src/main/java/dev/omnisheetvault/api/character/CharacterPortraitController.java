package dev.omnisheetvault.api.character;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
class CharacterPortraitController {

    private final CharacterPortraitService portraitService;

    CharacterPortraitController(CharacterPortraitService portraitService) {
        this.portraitService = portraitService;
    }

    @PutMapping("/api/characters/{id}/portrait")
    PortraitResponse choosePreset(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody ChoosePresetPortraitRequest request) {
        return portraitService.choosePreset(jwt, id, request.presetId());
    }

    @PostMapping(value = "/api/characters/{id}/portrait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    PortraitResponse upload(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestParam("file") MultipartFile file)
            throws IOException {
        return portraitService.upload(jwt, id, file.getBytes());
    }

    @DeleteMapping("/api/characters/{id}/portrait")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        portraitService.remove(jwt, id);
    }
}
