package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.ExtraSkill;

public record ExtraSkillResponse(String name, int bonus) {

    static ExtraSkillResponse from(ExtraSkill skill) {
        return new ExtraSkillResponse(skill.name(), skill.bonus());
    }
}
