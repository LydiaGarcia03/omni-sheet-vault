package dev.omnisheetvault.api.identity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** The authenticated player and the parts of their profile the app owns: shown name and profile picture. */
@RestController
class CurrentUserController {

    private final ProfileService profileService;

    CurrentUserController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/api/me")
    CurrentUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        return profileService.currentUser(jwt);
    }

    @PutMapping("/api/me/profile")
    CurrentUserResponse rename(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfileRequest request) {
        return profileService.rename(jwt, request.displayName());
    }

    @PostMapping(value = "/api/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    CurrentUserResponse uploadAvatar(@AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file) throws IOException {
        return profileService.uploadAvatar(jwt, file.getBytes());
    }

    @DeleteMapping("/api/me/avatar")
    CurrentUserResponse removeAvatar(@AuthenticationPrincipal Jwt jwt) {
        return profileService.removeAvatar(jwt);
    }

    /** A blank or null name goes back to the username. */
    record ProfileRequest(@Size(max = 40) String displayName) {
    }
}
