package dev.omnisheetvault.api.character;

/** What a not-yet-saved build plans and previews as, for a builder showing an option's effect before it is picked. */
public record DraftPreviewOnlyResponse(DraftResponse.PlanResponse plan, DraftPreviewResponse preview) {
}
