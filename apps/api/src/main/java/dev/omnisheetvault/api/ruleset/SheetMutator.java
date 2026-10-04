package dev.omnisheetvault.api.ruleset;

import java.util.Map;

/**
 * The session-state mutations every system supports — see architecture.md's
 * granularity guidance: one interface per behavior family, not one per mutation.
 * Every method takes the current sheet JSON and returns the updated sheet JSON;
 * persistence and recalculation are the orchestrator's job, not this seam's.
 */
public interface SheetMutator {

    String systemId();

    String applyDamage(String sheetJson, int amount);

    /** As {@link #applyDamage(String, int)}, from a critical hit (it matters to a character already at 0 hit points). */
    default String applyDamage(String sheetJson, int amount, boolean critical) {
        return applyDamage(sheetJson, amount);
    }

    String applyHealing(String sheetJson, int amount);

    /** Sets the death save counts by hand, as clicking the circles does (each clamped to the system's range); a no-op while not dying. */
    String setDeathSaves(String sheetJson, int successes, int failures);

    String clearDeathSaves(String sheetJson);

    /** Applies a death saving throw already rolled on a d20, by its natural result. */
    String applyDeathSaveRoll(String sheetJson, int naturalRoll);

    /** Sets the character's experience points; a negative total is floored at 0. Levels never change here. */
    String setExperiencePoints(String sheetJson, int points);

    /**
     * Sets the sheet's theme.
     *
     * @throws InvalidSheetThemeException if the system doesn't offer {@code theme}
     */
    String setSheetTheme(String sheetJson, String theme);

    /**
     * Sets one hand-set value over a calculated one (D&D Beyond's "Customize"): {@code group} names the kind of
     * customization, {@code target} what it applies to, and {@code valueJson} the system's value for it.
     *
     * @throws InvalidCustomizationException if the system doesn't offer it or the value is out of range
     */
    String customize(String sheetJson, String group, String target, String valueJson);

    /**
     * Clears one customization, or removes an entry the player added (such as a custom skill).
     *
     * @throws InvalidCustomizationException if the system doesn't offer it
     */
    String removeCustomization(String sheetJson, String group, String target);

    String setTemporaryHitPoints(String sheetJson, int amount);

    String toggleInspiration(String sheetJson);

    String toggleCondition(String sheetJson, String condition);

    /** {@code level} is clamped to [0, 6] — exhaustion has six levels, not a boolean like the other conditions. */
    String setExhaustionLevel(String sheetJson, int level);

    /** As {@link #toggleCondition(String, String)}, copying the condition's catalogue data (its mechanics) onto the sheet; null when the catalogue has none. */
    default String toggleCondition(String sheetJson, String condition, String conditionDataJson) {
        return toggleCondition(sheetJson, condition);
    }

    /** As {@link #setExhaustionLevel(String, int)}, copying the exhaustion levels' catalogue data onto the sheet; null when the catalogue has none. */
    default String setExhaustionLevel(String sheetJson, int level, String exhaustionDataJson) {
        return setExhaustionLevel(sheetJson, level);
    }

    /**
     * Returns the updated sheet JSON; the new item's generated key is not returned
     * separately. {@code storageLocation} is nullable — {@code null} defaults to the
     * system's own default bucket (D&D 5e: {@code EQUIPMENT}), same normalization
     * {@link #moveItem}'s target already goes through.
     */
    String addItem(
            String sheetJson, String name, int quantity, String cost, String notes, boolean requiresAttunement,
            String storageLocation);

    /**
     * Adds a real catalogue item — systems/dnd-5e/features/inventory-equipment-mechanics.md's slice 2
     * — copying {@code item}'s weapon/armour/charge data onto the sheet, same
     * "generic type crosses the seam" double duty {@link Spell} has for
     * {@link #learnSpell}. Unlike {@link #learnSpell}, always allowed: an item can be
     * carried in any quantity, so there is no cap to check and no no-op case for an
     * already-known slug (looting a second longsword is normal).
     *
     * @throws InvalidItemFieldException if a closed-set field names a value outside the system's own set
     */
    String addCatalogueItem(String sheetJson, Item item);

    /** As {@link #addCatalogueItem(String, Item)}, also copying the item's catalogue mechanics (magic-item effects); null when there are none. */
    default String addCatalogueItem(String sheetJson, Item item, String itemDataJson) {
        return addCatalogueItem(sheetJson, item);
    }

    String removeItem(String sheetJson, String itemKey);

    String toggleItemEquipped(String sheetJson, String itemKey);

    /**
     * @throws AttunementNotAllowedException if the item was never flagged {@code requiresAttunement}
     * @throws AttunementLimitExceededException if attuning would exceed the system's limit
     */
    String toggleItemAttuned(String sheetJson, String itemKey);

    /**
     * {@code quantity} is clamped to a minimum of 1 — dropping to zero is not how an item
     * is removed, that stays {@link #removeItem}'s job. An unknown {@code itemKey} is a
     * no-op, same treatment as {@link #toggleItemEquipped}'s unknown item key.
     */
    String setItemQuantity(String sheetJson, String itemKey, int quantity);

    /**
     * {@code storageLocation} is validated against the system's own set (a client error if
     * unknown), same treatment as {@code denomination} below. An unknown {@code itemKey} is a
     * no-op, same treatment as {@link #toggleItemEquipped}'s unknown item key.
     */
    String moveItem(String sheetJson, String itemKey, String storageLocation);

    /** Not locked at creation — editable at any time, same as D&D Beyond's own equivalent setting. */
    String setTrackEncumbrance(String sheetJson, boolean trackEncumbrance);

    /** {@code denomination} is validated against the system's own set; an unknown one is a client error. */
    String addCoins(String sheetJson, String denomination, int amount);

    /** Never drops a denomination's total below zero. */
    String removeCoins(String sheetJson, String denomination, int amount);

    /**
     * Same damage/heal/temporary-hit-points rules as the character's own, scoped to
     * one extra by key. An unknown {@code extraKey} is a no-op, matching
     * {@link #toggleItemEquipped}'s treatment of an unknown item key.
     */
    String applyExtraDamage(String sheetJson, String extraKey, int amount);

    String applyExtraHealing(String sheetJson, String extraKey, int amount);

    String setExtraTemporaryHitPoints(String sheetJson, String extraKey, int amount);

    /** Removing a key that is not present is a no-op, same treatment as {@link #removeItem}. */
    String removeExtra(String sheetJson, String extraKey);

    /**
     * Spends one use of a limited-use feature action, capped at its own
     * {@code maxUses}. An unknown {@code featureKey} is a no-op, same treatment as
     * {@link #toggleItemEquipped}'s unknown item key. Feature actions and feature
     * traits are independent lists (see {@code Dnd5eFeatureTrait}'s doc comment) —
     * spending a use here never affects the corresponding feature trait entry.
     */
    String useFeatureAction(String sheetJson, String featureKey);

    /** Restores one use, floored at zero. */
    String restoreFeatureAction(String sheetJson, String featureKey);

    /** Same rules as {@link #useFeatureAction}, scoped to the feature traits list. */
    String useFeatureTraitUse(String sheetJson, String featureKey);

    String restoreFeatureTraitUse(String sheetJson, String featureKey);

    /**
     * Applies a hit-dice spend already rolled and validated by the caller: heals
     * {@code healAmount} (capped at the sheet's own max hit points, same rule as
     * {@link #applyHealing}) and records {@code count} more hit dice as used. This
     * method does no rolling and no availability check — see {@code RollService}
     * in the {@code dice} package, which resolves and validates the spend before
     * calling here, so an already-persisted roll is never left orphaned by a
     * rejected mutation.
     */
    String spendHitDice(String sheetJson, int count, int healAmount);

    /** As {@link #spendHitDice(String, int, int)}, spending dice of one size (a multiclass character has several). */
    default String spendHitDice(String sheetJson, int dieSize, int count, int healAmount) {
        return spendHitDice(sheetJson, count, healAmount);
    }

    /**
     * Spends one slot at {@code level}, capped at zero. An unknown {@code level}
     * (no track at that level) is a no-op, same treatment as
     * {@link #toggleItemEquipped}'s unknown item key. Unlike {@link #spendHitDice},
     * this is a plain resource spend with no roll attached, so it clamps instead
     * of throwing when the level is already exhausted — same treatment as
     * {@link #useFeatureAction}'s box track. {@code pact} picks the separate pool
     * (D&D 5e Pact Magic) that may share the level.
     */
    String consumeSpellSlot(String sheetJson, int level, boolean pact);

    /** Restores one slot at {@code level} in the regular or the {@code pact} pool, capped at its own max. */
    String restoreSpellSlot(String sheetJson, int level, boolean pact);

    /**
     * Spends an item-granted spell's own charge cost — systems/dnd-5e/features/
     * inventory-equipment-mechanics.md's slice 7 (the Wand of Fireballs
     * mechanic). {@code spellKey} identifies the granted spell entry (not the
     * item itself); an unknown key, or one that isn't item-granted, is a no-op.
     * Clamps at the owning item's own maximum charges rather than throwing, same
     * philosophy as {@link #consumeSpellSlot}.
     */
    String castItemGrantedSpell(String sheetJson, String spellKey);

    /**
     * Casts a known spell: spends one slot at {@code slotLevel} (0 spends none; {@code pact} spends from the
     * Pact Magic pool), ends
     * any concentration effect when this spell needs concentration, and starts the
     * spell's lasting effect, read from {@code spellDataJson} (the spell's catalogue
     * data; null when the catalogue has none). A spell with no lasting effect only
     * spends the slot. An effect cast on an ally ({@code onSelf} false) is tracked
     * but changes none of the character's own values.
     */
    String castSpell(String sheetJson, String spellKey, int slotLevel, boolean pact, String spellDataJson, boolean onSelf);

    /** Ends one active effect; an unknown key is a no-op. */
    String endActiveEffect(String sheetJson, String effectKey);

    /**
     * Restores every feature action/trait whose recharge trigger matches a short
     * rest, and ends the active effects whose source says a short rest ends them.
     * Hit dice and spell slots are long-rest-only resources in 5e, so a short rest
     * leaves them untouched.
     */
    String applyShortRest(String sheetJson);

    /**
     * Restores everything {@link #applyShortRest} does, plus current hit points
     * to the sheet's own max, every spell slot to its own max, and spent hit
     * dice — PHB: half the character's total hit dice, rounded down, minimum
     * one. Ends the active effects whose source says a long rest ends them.
     */
    String applyLongRest(String sheetJson);

    /**
     * As {@link #applyLongRest(String)}, with the player choosing which spent hit dice come
     * back ({@code hitDiceRecovered}: die size → count; null for the system's default).
     *
     * @throws InvalidHitDiceRecoveryException if the choice recovers more than the rest allows
     */
    default String applyLongRest(String sheetJson, Map<Integer, Integer> hitDiceRecovered) {
        return applyLongRest(sheetJson);
    }

    /**
     * Adds {@code spell} to the character's known spells, newly unprepared and
     * not always-prepared — phase 9's "Manage spells". A no-op if a spell with
     * the same key is already known, so learning the same catalogue entry twice never
     * duplicates it. Otherwise checked against the owning class's own
     * cantrip/known-spell cap.
     *
     * @throws SpellLimitExceededException if learning would exceed that cap
     */
    String learnSpell(String sheetJson, Spell spell);

    /** An unknown {@code spellKey} is a no-op, same treatment as an unknown item key. */
    String removeSpell(String sheetJson, String spellKey);

    /**
     * Adds {@code feat} to the character's feature traits (category {@code FEAT}).
     * A no-op if a feat with the same key is already known, same treatment as
     * {@link #learnSpell}; unlike a spell, a feat has no per-class cap to check.
     */
    String learnFeat(String sheetJson, FeatureTrait feat);

    /** An unknown {@code featureKey} is a no-op, same treatment as an unknown spell key. */
    String removeFeat(String sheetJson, String featureKey);

    /**
     * Marks a known leveled spell prepared, within the owning class's own
     * {@code spellsPreparedMax}. A no-op — never an error — for a cantrip, an
     * already-{@code alwaysPrepared} spell, a spell under a {@code KNOWN}-type
     * class (no separate preparation step exists there), or an unknown
     * {@code spellKey}; none of those are a player mistake worth surfacing.
     *
     * @throws SpellPreparationLimitExceededException if preparing would exceed the cap
     */
    String prepareSpell(String sheetJson, String spellKey);

    /** Restores one prepared slot, same no-op cases as {@link #prepareSpell}, no limit to check. */
    String unprepareSpell(String sheetJson, String spellKey);

    /**
     * Sets one background/characteristics/notes field — phase 10. {@code field}
     * is validated against the system's own editable field set; an unknown one
     * is a client error.
     * The background's identity fields (which background is chosen, its
     * feature) are never editable through this seam — see
     * {@code Dnd5eBackground}'s doc comment for why.
     *
     * @throws InvalidBackgroundFieldException if {@code field} isn't editable
     */
    String updateBackgroundField(String sheetJson, String field, String value);

    /**
     * Adds a player-authored custom action — punch list item 7
     * (systems/dnd-5e/references/sheet-fidelity-audit.md). Returns the updated sheet JSON; the
     * new action's generated key is not returned separately, same treatment as
     * {@link #addItem}.
     *
     * @throws InvalidCustomActionFieldException if a closed-set field names a value outside the system's own set
     */
    String addCustomAction(String sheetJson, CustomAction action);

    /**
     * Replaces a custom action's fields, keeping its key; an unknown key is a no-op.
     *
     * @throws InvalidCustomActionFieldException if a closed-set field names a value outside the system's own set
     */
    String updateCustomAction(String sheetJson, String actionKey, CustomAction action);

    /** Removing a key that is not present is a no-op, same treatment as {@link #removeItem}. */
    String removeCustomAction(String sheetJson, String actionKey);
}
