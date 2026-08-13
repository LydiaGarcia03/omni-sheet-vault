package dev.omnisheetvault.api.identity;

import java.util.List;

public record CurrentUserResponse(String subject, String username, String email, List<String> roles) {
}
