package dev.omnisheetvault.api.identity;

import java.util.List;
import java.util.UUID;

public record CurrentUserResponse(UUID id, String subject, String displayName, String email, List<String> roles) {
}
