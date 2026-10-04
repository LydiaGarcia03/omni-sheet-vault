package dev.omnisheetvault.api.character;

import tools.jackson.databind.JsonNode;

/** A version-controlled build file ({@code content/<system>/builds/*.json}): the character's name, its system, and that system's own build. */
record CharacterBuildFile(String characterName, String systemId, JsonNode build) {
}
