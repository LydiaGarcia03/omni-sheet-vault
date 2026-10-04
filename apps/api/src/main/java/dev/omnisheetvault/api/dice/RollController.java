package dev.omnisheetvault.api.dice;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RollController {

    private final RollService rollService;

    RollController(RollService rollService) {
        this.rollService = rollService;
    }

    @PostMapping("/api/characters/{id}/rolls")
    @ResponseStatus(HttpStatus.CREATED)
    RollResponse roll(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody RollRequest request) {
        return RollResponse.from(rollService.roll(jwt, id, request));
    }

    @PostMapping("/api/characters/{id}/rolls/manual")
    @ResponseStatus(HttpStatus.CREATED)
    RollResponse manualRoll(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody ManualRollRequest request) {
        return RollResponse.from(rollService.manualRoll(jwt, id, request));
    }

    @PostMapping("/api/characters/{id}/rolls/creation")
    @ResponseStatus(HttpStatus.CREATED)
    RollResponse creationRoll(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody CreationRollRequest request) {
        return RollResponse.from(rollService.creationRoll(jwt, id, request));
    }

    @GetMapping("/api/characters/{id}/rolls")
    List<RollResponse> history(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return rollService.history(jwt, id).stream().map(RollResponse::from).toList();
    }
}
