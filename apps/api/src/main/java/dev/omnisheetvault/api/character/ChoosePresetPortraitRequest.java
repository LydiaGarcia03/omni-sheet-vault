package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

record ChoosePresetPortraitRequest(@NotBlank @Pattern(regexp = "[0-9]{1,12}") String presetId) {
}
