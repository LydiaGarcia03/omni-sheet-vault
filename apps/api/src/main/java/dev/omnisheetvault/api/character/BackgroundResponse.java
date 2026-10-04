package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.Background;

public record BackgroundResponse(
        String name,
        String featureName,
        String featureDescription,
        String alignment,
        String personalityTraits,
        String ideals,
        String bonds,
        String flaws,
        String appearance,
        String organizations,
        String allies,
        String enemies,
        String backstory,
        String other,
        String gender,
        String eyes,
        String size,
        String height,
        String faith,
        String hair,
        String skin,
        String age,
        String weight,
        String lifestyle) {

    static BackgroundResponse from(Background background) {
        return new BackgroundResponse(
                background.name(), background.featureName(), background.featureDescription(),
                background.alignment(), background.personalityTraits(), background.ideals(), background.bonds(),
                background.flaws(), background.appearance(), background.organizations(), background.allies(),
                background.enemies(), background.backstory(), background.other(), background.gender(),
                background.eyes(), background.size(), background.height(), background.faith(), background.hair(),
                background.skin(), background.age(), background.weight(), background.lifestyle());
    }
}
