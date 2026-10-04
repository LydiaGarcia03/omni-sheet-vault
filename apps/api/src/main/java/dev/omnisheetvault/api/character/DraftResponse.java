package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.CreationChoiceOption;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** A creation draft: its build document as stored, plus every choice that build offers; {@code portrait} is null when unset. */
public record DraftResponse(
        UUID id, String name, String systemId, JsonNode build, PlanResponse plan, DraftPreviewResponse preview, PortraitResponse portrait) {

    public record PlanResponse(List<ChoiceResponse> choices, List<String> problems, int pendingCount) {

        static PlanResponse from(BuildPlan plan) {
            return new PlanResponse(plan.choices().stream().map(ChoiceResponse::from).toList(), plan.problems(), plan.pending().size());
        }
    }

    /** {@code group} and {@code level} file the choice under a class and level (both null when it has no placement). */
    public record ChoiceResponse(
            String id, String type, String parentChoiceId, String prompt, String sourceLabel, int count,
            boolean optional, boolean pending, List<OptionResponse> options, List<String> selected, String group, Integer level) {

        static ChoiceResponse from(CreationChoice choice) {
            return new ChoiceResponse(choice.id(), choice.type(), choice.parentChoiceId(), choice.prompt(), choice.sourceLabel(),
                    choice.count(), choice.optional(), choice.isPending(),
                    choice.options().stream().map(OptionResponse::from).toList(), choice.selected(),
                    choice.placement() == null ? null : choice.placement().group(),
                    choice.placement() == null ? null : choice.placement().level());
        }
    }

    public record OptionResponse(String key, String label, String sourceBook, String summary, JsonNode data) {

        static OptionResponse from(CreationChoiceOption option) {
            return new OptionResponse(option.key(), option.label(), option.sourceBook(), option.summary(), option.data());
        }
    }

    static DraftResponse from(CharacterDraftService.DraftView view, ObjectMapper objectMapper, PortraitResponse portrait) {
        Character draft = view.character();
        return new DraftResponse(draft.id(), draft.name(), draft.systemId(), objectMapper.readTree(draft.creationDraft()),
                PlanResponse.from(view.plan()), DraftPreviewResponse.from(view.preview(), view.vitals()), portrait);
    }
}
