package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.ClassLevelInfo;
import dev.omnisheetvault.api.ruleset.Experience;
import java.util.List;

record ExperienceResponse(
        String advancement,
        int points,
        int level,
        int levelFromPoints,
        int currentLevelAt,
        Integer nextLevelAt,
        boolean levelUpAvailable,
        boolean canLevelUp,
        List<Integer> thresholds,
        List<ClassLevelResponse> classes) {

    record ClassLevelResponse(String name, String subclass, int level) {

        static ClassLevelResponse from(ClassLevelInfo classLevel) {
            return new ClassLevelResponse(classLevel.name(), classLevel.subclass(), classLevel.level());
        }
    }

    static ExperienceResponse from(Experience experience) {
        if (experience == null) {
            return null;
        }
        return new ExperienceResponse(experience.advancement(), experience.points(), experience.level(),
                experience.levelFromPoints(), experience.currentLevelAt(), experience.nextLevelAt(),
                experience.levelUpAvailable(), experience.canLevelUp(), experience.thresholds(),
                experience.classes().stream().map(ClassLevelResponse::from).toList());
    }
}
