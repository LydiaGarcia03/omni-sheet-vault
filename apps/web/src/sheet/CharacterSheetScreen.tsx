import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router';
import { CharacterPortrait } from '../characters/CharacterPortrait';
import type { BackgroundCatalogueEntryData, CatalogueEntry } from '../catalogue/api';
import { getCatalogue } from '../catalogue/api';
import { renameCharacter, type Character } from '../characters/api';
import { getRollHistory, postManualRoll, postRoll, type DiceGroup, type RollContextMenuHandler, type RollKind, type RollResult } from '../dice/api';
import { CustomRollPicker } from '../dice/CustomRollPicker';
import { DiceTray } from '../dice/DiceTray';
import { RollModeMenu, type RollModeRequest } from '../dice/RollModeMenu';
import '../dice/dice.css';
import { customizationsOf } from '../systems/dnd5e/customizations';
import { buildItemDetailRequest, hasBagOfHoldingIn } from '../systems/dnd5e/ItemRow';
import { CONDITIONS } from '../systems/dnd5e/DefensesConditionsPanel';
import { Dnd5eVitalsColumns } from '../systems/dnd5e/Dnd5eVitalsColumns';
import { Dnd5eVitalsTopRow } from '../systems/dnd5e/Dnd5eVitalsTopRow';
import { FrameIcon } from '../systems/dnd5e/FrameIcon';
import conditionBlindedSvg from '../systems/dnd5e/frames/dnd_icon_condition_blinded.svg?raw';
import conditionCharmedSvg from '../systems/dnd5e/frames/dnd_icon_condition_charmed.svg?raw';
import conditionDeafenedSvg from '../systems/dnd5e/frames/dnd_icon_condition_deafened.svg?raw';
import conditionExhaustedSvg from '../systems/dnd5e/frames/dnd_icon_condition_exhausted.svg?raw';
import conditionFrightenedSvg from '../systems/dnd5e/frames/dnd_icon_condition_frightened.svg?raw';
import conditionGrappledSvg from '../systems/dnd5e/frames/dnd_icon_condition_grappled.svg?raw';
import conditionIncapacitatedSvg from '../systems/dnd5e/frames/dnd_icon_condition_incapacitated.svg?raw';
import conditionInvisibleSvg from '../systems/dnd5e/frames/dnd_icon_condition_invisible.svg?raw';
import conditionParalyzedSvg from '../systems/dnd5e/frames/dnd_icon_condition_paralyzed.svg?raw';
import conditionPetrifiedSvg from '../systems/dnd5e/frames/dnd_icon_condition_petrified.svg?raw';
import conditionPoisonedSvg from '../systems/dnd5e/frames/dnd_icon_condition_poisoned.svg?raw';
import conditionProneSvg from '../systems/dnd5e/frames/dnd_icon_condition_prone.svg?raw';
import conditionRestrainedSvg from '../systems/dnd5e/frames/dnd_icon_condition_restrained.svg?raw';
import conditionStunnedSvg from '../systems/dnd5e/frames/dnd_icon_condition_stunned.svg?raw';
import conditionUnconsciousSvg from '../systems/dnd5e/frames/dnd_icon_condition_unconscious.svg?raw';
import gameLogIconSvg from '../systems/dnd5e/frames/dnd_icon_game_log.svg?raw';
import longRestIconSvg from '../systems/dnd5e/frames/dnd_icon_long_rest.svg?raw';
import shortRestIconSvg from '../systems/dnd5e/frames/dnd_icon_short_rest.svg?raw';
import '../systems/dnd5e/themes.css';
import '../systems/dnd5e/frames.css';
import { LastingEffectSpellsContext, lastingEffectSpellSlugs } from '../systems/dnd5e/lastingEffectSpells';
import { LongRestBody, restRecovery, ShortRestBody } from '../systems/dnd5e/RestBody';
import { customActionDetailRequest } from '../systems/dnd5e/customActionDetail';
import {
  CONDITION_RULES_TEXT,
  DEATH_SAVES_RULES_TEXT,
  EXHAUSTION_RULES_TEXT,
  LONG_REST_INTRO_TEXT,
  LONG_REST_RULES_TEXT,
  SHORT_REST_INTRO_TEXT,
  SHORT_REST_RULES_TEXT,
} from '../systems/dnd5e/rulesText';
import {
  getCharacterSheet,
  postLongRest,
  postMutationAction,
  postShortRest,
  postDeathSaveRoll,
  postUpdateBackgroundField,
  type BackgroundField,
  type CharacterSheet,
  type EntityDetailRequest,
  type ExplainerRequest,
  type PaneRequest,
  type HitDiceBySize,
  type MutationAction,
  type SidebarContent,
  type TextFieldRequest,
} from './api';
import { SheetShell } from './SheetShell';
import { Sidebar } from './Sidebar';
import { useSheetTheme } from './useSheetTheme';
import { XpBar } from './XpBar';
import { DEFAULT_SHEET_THEME, SHEET_THEMES } from '../systems/dnd5e/sheetThemes';
import { ThemeSample } from '../systems/dnd5e/ThemeSample';

type CharacterSheetScreenProps = {
  character: Character;
  userLabel: string;
  onBack: () => void;
};

/** The HP Management panel's live values: the hit points and the Max HP Modifier / Override Max HP. */
function hpManagementValues(sheet: CharacterSheet) {
  const hitPoints = customizationsOf(sheet).hitPoints;
  return {
    current: sheet.currentHitPoints,
    max: sheet.hitPoints.value,
    temporary: sheet.temporaryHitPoints,
    calculatedMax: sheet.calculatedMaxHitPoints,
    maxModifier: hitPoints.maxModifier?.value ?? null,
    maxOverride: hitPoints.overrideMax?.value ?? null,
    maxContributions: sheet.hitPoints.contributions,
    dying: sheet.deathSaves?.dying ?? false,
    deathSavesRulesText: DEATH_SAVES_RULES_TEXT,
  };
}

function hitPointsCustomization(field: string, value: number | null): MutationAction {
  return value === null
    ? { type: 'REMOVE_CUSTOMIZATION', group: 'hitPoints', target: field }
    : { type: 'CUSTOMIZE', group: 'hitPoints', target: field, value: { value, notes: null } };
}

const CONDITION_ICONS: Record<string, string> = {
  blinded: conditionBlindedSvg,
  charmed: conditionCharmedSvg,
  deafened: conditionDeafenedSvg,
  frightened: conditionFrightenedSvg,
  grappled: conditionGrappledSvg,
  incapacitated: conditionIncapacitatedSvg,
  invisible: conditionInvisibleSvg,
  paralyzed: conditionParalyzedSvg,
  petrified: conditionPetrifiedSvg,
  poisoned: conditionPoisonedSvg,
  prone: conditionProneSvg,
  restrained: conditionRestrainedSvg,
  stunned: conditionStunnedSvg,
  unconscious: conditionUnconsciousSvg,
};

function conditionEntries(sheet: CharacterSheet) {
  return CONDITIONS.map(({ key, label }) => ({
    key,
    label,
    active: sheet.activeConditions.includes(key),
    icon: CONDITION_ICONS[key] && <FrameIcon svg={CONDITION_ICONS[key]} aria-hidden />,
    rulesText: CONDITION_RULES_TEXT[key] ?? '',
  }));
}

/**
 * Only one system module exists today, so it is composed directly here. Dispatching
 * by systemId to the right module is a phase-11 concern — no point building that
 * seam before a second system proves what it needs.
 */
/** Marks the sidebar as shown by the player while no panel is open. */
const SHOW_EMPTY = Symbol('show empty sidebar');

export function CharacterSheetScreen({ character, userLabel, onBack }: CharacterSheetScreenProps) {
  const [sheet, setSheet] = useState<CharacterSheet | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [rollHistory, setRollHistory] = useState<RollResult[]>([]);
  // Unlike rollHistory (server-fetched on mount, so rollHistory[0] can be a roll from a
  // past session), this only ever gets set by a roll made during this page's own
  // lifetime, so the DiceTray it feeds has nothing to show right after a fresh load or
  // reload — direct owner report, 2026-09-20.
  const [sessionLatestRoll, setSessionLatestRoll] = useState<RollResult | null>(null);
  const [rollError, setRollError] = useState<string | null>(null);
  const [rollModeRequest, setRollModeRequest] = useState<RollModeRequest | null>(null);
  const [mutationError, setMutationError] = useState<string | null>(null);
  const [characterName, setCharacterName] = useState(character.name);
  const handleRename = useCallback(
    (name: string) => {
      const previous = characterName;
      setCharacterName(name);
      setMutationError(null);
      renameCharacter(character.id, name)
        .then((renamed) => setCharacterName(renamed.name))
        .catch(() => {
          setCharacterName(previous);
          setMutationError('Renaming failed to reach the server. Nothing was saved.');
        });
    },
    [character.id, characterName],
  );
  const [sidebarContent, setSidebarContent] = useState<SidebarContent | null>(null);
  // The panel the player hid (or SHOW_EMPTY after showing an empty sidebar): any newly opened panel is shown,
  // and with no panel the sidebar stays collapsed unless the player shows it.
  const [hiddenPanel, setHiddenPanel] = useState<SidebarContent | typeof SHOW_EMPTY | null>(null);
  const sidebarHidden = sidebarContent === null ? hiddenPanel !== SHOW_EMPTY : hiddenPanel === sidebarContent;
  const hideSidebar = useCallback(() => setHiddenPanel(sidebarContent), [sidebarContent]);
  const showSidebar = useCallback(() => setHiddenPanel(SHOW_EMPTY), []);
  const [catalogueSpells, setCatalogueSpells] = useState<CatalogueEntry[]>([]);
  const [catalogueBackgrounds, setCatalogueBackgrounds] = useState<CatalogueEntry[]>([]);
  const [catalogueFeats, setCatalogueFeats] = useState<CatalogueEntry[]>([]);
  const [catalogueItems, setCatalogueItems] = useState<CatalogueEntry[]>([]);

  useEffect(() => {
    getCharacterSheet(character.id)
      .then(setSheet)
      .catch(() => setError('Could not load the character sheet.'));
  }, [character.id]);

  useEffect(() => {
    getRollHistory(character.id)
      .then(setRollHistory)
      .catch(() => {
        // The game log is a convenience, not the roll itself — a failed history
        // fetch leaves the log empty rather than blocking the sheet.
      });
  }, [character.id]);

  useEffect(() => {
    getCatalogue(character.systemId, 'SPELL')
      .then(setCatalogueSpells)
      .catch(() => {
        // "Manage spells" degrades to an empty picker rather than blocking the sheet.
      });
  }, [character.systemId]);

  useEffect(() => {
    getCatalogue(character.systemId, 'BACKGROUND')
      .then(setCatalogueBackgrounds)
      .catch(() => {
        // Personality/Ideals/Bonds/Flaws suggestions degrade to "none" rather than blocking the sheet.
      });
  }, [character.systemId]);

  useEffect(() => {
    getCatalogue(character.systemId, 'FEAT')
      .then(setCatalogueFeats)
      .catch(() => {
        // "Manage feats" degrades to an empty picker rather than blocking the sheet.
      });
  }, [character.systemId]);

  useEffect(() => {
    getCatalogue(character.systemId, 'ITEM')
      .then(setCatalogueItems)
      .catch(() => {
        // "Manage Inventory" degrades to custom-item-only rather than blocking the sheet.
      });
  }, [character.systemId]);

  // Matched by slug (kebab-cased background name) against the catalogue's backgrounds; a custom
  // background with no catalogue entry just gets no suggestions table.
  const backgroundSuggestions = useMemo<BackgroundCatalogueEntryData | null>(() => {
    if (!sheet) {
      return null;
    }
    const slug = sheet.background.name.trim().toLowerCase().replace(/\s+/g, '-');
    const entry = catalogueBackgrounds.find((candidate) => candidate.slug === slug);
    return entry ? (entry.data as BackgroundCatalogueEntryData) : null;
  }, [sheet, catalogueBackgrounds]);

  const lastingEffectSpells = useMemo(() => lastingEffectSpellSlugs(catalogueSpells), [catalogueSpells]);

  const handleRoll = useCallback(
    (kind: RollKind, key: string | null, castAtLevel?: number, mode?: { advantage?: boolean; disadvantage?: boolean }) => {
      setRollError(null);
      postRoll(character.id, kind, key, mode, castAtLevel)
        .then((result) => {
          setRollHistory((history) => [result, ...history]);
          setSessionLatestRoll(result);
        })
        .catch(() => setRollError('Roll failed to reach the server. Nothing was recorded.'));
    },
    [character.id],
  );

  const handleRollContextMenu = useCallback<RollContextMenuHandler>((x, y, kind, key, formattedModifier) => {
    setRollModeRequest({ x, y, kind, key, formattedModifier });
  }, []);

  const handleManualRoll = useCallback(
    (dice: DiceGroup[]) => {
      setRollError(null);
      postManualRoll(character.id, dice)
        .then((result) => {
          setRollHistory((history) => [result, ...history]);
          setSessionLatestRoll(result);
        })
        .catch(() => setRollError('Roll failed to reach the server. Nothing was recorded.'));
    },
    [character.id],
  );

  const handleMutate = useCallback(
    (action: MutationAction) => {
      setMutationError(null);
      const previousSheet = sheet;

      // Toggles are rule-free, so flipping them locally is safe. Hit point amounts are
      // not: damage consumes temporary points first and healing caps at the maximum,
      // rules that live only in the API (ground-rules.md forbids duplicating them here) —
      // those wait for the server's own recalculated sheet instead of a local guess.
      if (previousSheet && action.type === 'TOGGLE_INSPIRATION') {
        setSheet({ ...previousSheet, heroicInspiration: !previousSheet.heroicInspiration });
      } else if (previousSheet && action.type === 'TOGGLE_CONDITION') {
        const isActive = previousSheet.activeConditions.includes(action.condition);
        setSheet({
          ...previousSheet,
          activeConditions: isActive
            ? previousSheet.activeConditions.filter((condition) => condition !== action.condition)
            : [...previousSheet.activeConditions, action.condition],
        });
      } else if (previousSheet && action.type === 'TOGGLE_ITEM_EQUIPPED') {
        setSheet({
          ...previousSheet,
          items: previousSheet.items.map((item) =>
            item.key === action.itemKey ? { ...item, equipped: !item.equipped } : item,
          ),
        });
      } else if (previousSheet && action.type === 'TOGGLE_ITEM_ATTUNED') {
        setSheet({
          ...previousSheet,
          items: previousSheet.items.map((item) =>
            item.key === action.itemKey ? { ...item, attuned: !item.attuned } : item,
          ),
        });
      }

      postMutationAction(character.id, action)
        .then(setSheet)
        .catch(() => {
          setSheet(previousSheet);
          setMutationError('Change failed to reach the server. Nothing was saved.');
        });
    },
    [character.id, sheet],
  );

  // Rolled server-side: the sheet and the roll come back together.
  const handleRollDeathSave = useCallback(() => {
    setRollError(null);
    setMutationError(null);
    postDeathSaveRoll(character.id)
      .then(({ sheet: updatedSheet, roll }) => {
        setSheet(updatedSheet);
        setRollHistory((history) => [roll, ...history]);
        setSessionLatestRoll(roll);
      })
      .catch(() => setMutationError('Death save failed to reach the server. Nothing was saved.'));
  }, [character.id]);

  // A short rest optionally spends hit dice in the same operation (see
  // postShortRest's doc comment) — closes the sidebar on success, same as a
  // long rest, since a rest is a one-shot action, not something left open to
  // keep editing.
  const handleTakeShortRest = useCallback(
    (hitDiceBySize: HitDiceBySize) => {
      setRollError(null);
      setMutationError(null);
      postShortRest(character.id, hitDiceBySize)
        .then(({ sheet: updatedSheet, rolls }) => {
          setSheet(updatedSheet);
          if (rolls.length > 0) {
            setRollHistory((history) => [...[...rolls].reverse(), ...history]);
            setSessionLatestRoll(rolls[rolls.length - 1]);
          }
          setSidebarContent(null);
        })
        .catch(() => setMutationError('Short rest failed to reach the server. Nothing was saved.'));
    },
    [character.id],
  );

  const handleTakeLongRest = useCallback((hitDiceRecovered?: HitDiceBySize) => {
    setMutationError(null);
    postLongRest(character.id, hitDiceRecovered)
      .then((updatedSheet) => {
        setSheet(updatedSheet);
        setSidebarContent(null);
      })
      .catch(() => setMutationError('Long rest failed to reach the server. Nothing was saved.'));
  }, [character.id]);

  const handleOpenShortRest = useCallback(() => {
    if (!sheet) {
      return;
    }
    setSidebarContent({
      kind: 'mechanic',
      title: 'Short Rest',
      summary: SHORT_REST_INTRO_TEXT,
      mutedSummary: true,
      rulesText: SHORT_REST_RULES_TEXT,
      body: (
        <ShortRestBody
          hitDice={sheet.hitDice}
          conModifier={sheet.abilityModifiers.constitution?.value ?? 0}
          recover={restRecovery(sheet, 'short')}
          onConfirm={handleTakeShortRest}
        />
      ),
    });
  }, [sheet, handleTakeShortRest]);

  const handleOpenLongRest = useCallback(() => {
    setSidebarContent({
      kind: 'mechanic',
      title: 'Long Rest',
      summary: LONG_REST_INTRO_TEXT,
      rulesText: LONG_REST_RULES_TEXT,
      body: sheet ? (
        <LongRestBody
          hitDice={sheet.hitDice}
          conModifier={sheet.abilityModifiers.constitution?.value ?? 0}
          recover={restRecovery(sheet, 'long')}
          onConfirm={handleTakeLongRest}
        />
      ) : null,
    });
  }, [sheet, handleTakeLongRest]);

  const handleExplain = useCallback((request: ExplainerRequest) => setSidebarContent(request), []);
  const handleOpenPane = useCallback((request: PaneRequest) => setSidebarContent(request), []);
  const handleOpenDetail = useCallback((request: EntityDetailRequest) => setSidebarContent(request), []);
  const handleOpenLog = useCallback(
    () => setSidebarContent({ kind: 'log', entries: rollHistory, senderName: characterName }),
    [rollHistory, characterName],
  );

  // Built from live sheet state at open time, then kept in sync by
  // displayedSidebarContent below — the same treatment spellManagement gets, since
  // the inline Hit Points box's own Heal/Damage/Temp controls can mutate the sheet
  // while this panel stays open.
  const handleOpenHpManagement = useCallback(() => {
    if (!sheet) {
      return;
    }
    setSidebarContent({
      kind: 'hpManagement',
      ...hpManagementValues(sheet),
      onDamage: (amount) => handleMutate({ type: 'DAMAGE', amount }),
      onHeal: (amount) => handleMutate({ type: 'HEAL', amount }),
      onSetTemporary: (amount) => handleMutate({ type: 'TEMPORARY_HIT_POINTS', amount }),
      onSetMaxModifier: (value) => handleMutate(hitPointsCustomization('maxModifier', value)),
      onSetMaxOverride: (value) => handleMutate(hitPointsCustomization('overrideMax', value)),
    });
  }, [sheet, handleMutate]);

  const navigate = useNavigate();
  const handleLevelUp = useCallback(() => navigate(`/characters/${character.id}/level-up`), [navigate, character.id]);

  useSheetTheme(sheet?.sheetTheme ?? null);

  // `current` is refreshed from the live sheet on every render (displayedSidebarContent below).
  const handleOpenChangeAppearance = useCallback(() => {
    setSidebarContent({
      kind: 'changeAppearance',
      themes: SHEET_THEMES,
      current: sheet?.sheetTheme ?? DEFAULT_SHEET_THEME,
      onSelect: (theme) => handleMutate({ type: 'SET_SHEET_THEME', theme }),
      renderSample: (theme) => <ThemeSample color={theme.color} />,
      portrait: <CharacterPortrait portrait={character.portrait} systemId={character.systemId} className="change-appearance__portrait" alt="" />,
    });
  }, [sheet, handleMutate, character.portrait, character.systemId]);

  // Experience is refreshed from the live sheet on every render (displayedSidebarContent below).
  const handleOpenCharacterMenu = useCallback(() => {
    if (!sheet) {
      return;
    }
    setSidebarContent({
      kind: 'characterMenu',
      name: characterName,
      portrait: <CharacterPortrait portrait={character.portrait} systemId={character.systemId} className="character-menu__portrait-image" alt="" />,
      subtitle: character.summary.slice(1).map((fact) => fact.value).join(' · ') || null,
      experience: sheet.experience ?? null,
      onManageExperience: () => handleOpenManageExperienceRef.current(),
      onLevelUp: handleLevelUp,
      onOpenLog: handleOpenLog,
      onOpenShortRest: handleOpenShortRest,
      onOpenLongRest: handleOpenLongRest,
      onChangeAppearance: handleOpenChangeAppearance,
      onRename: handleRename,
      icons: {
        shortRest: <FrameIcon svg={shortRestIconSvg} className="rest-button__icon" />,
        longRest: <FrameIcon svg={longRestIconSvg} className="rest-button__icon" />,
        gameLog: <FrameIcon svg={gameLogIconSvg} className="rest-button__icon" />,
      },
    });
  }, [sheet, character, characterName, handleRename, handleLevelUp, handleOpenLog, handleOpenShortRest, handleOpenLongRest, handleOpenChangeAppearance]);

  const handleOpenManageExperience = useCallback(() => {
    if (!sheet?.experience) {
      return;
    }
    setSidebarContent({
      kind: 'manageExperience',
      experience: sheet.experience,
      onApply: (points) => {
        handleMutate({ type: 'SET_EXPERIENCE', points });
        handleOpenCharacterMenu();
      },
    });
  }, [sheet, handleMutate, handleOpenCharacterMenu]);
  const handleOpenManageExperienceRef = useRef(handleOpenManageExperience);
  handleOpenManageExperienceRef.current = handleOpenManageExperience;

  // classes/knownSpells are re-derived from live sheet state on every render (see
  // displayedSidebarContent below), same treatment as the collection editor's
  // sections — only the catalogue and the handlers are fixed at open time.
  const handleOpenSpellManagement = useCallback(() => {
    if (!sheet) {
      return;
    }
    setSidebarContent({
      kind: 'spellManagement',
      classes: sheet.spellcasting,
      knownSpells: sheet.spells,
      slots: sheet.spellSlots,
      catalogue: catalogueSpells,
      onLearn: (catalogueEntryId, className) => handleMutate({ type: 'LEARN_SPELL', catalogueEntryId, className }),
      onRemove: (spellKey) => handleMutate({ type: 'REMOVE_SPELL', spellKey }),
      onPrepare: (spellKey) => handleMutate({ type: 'PREPARE_SPELL', spellKey }),
      onUnprepare: (spellKey) => handleMutate({ type: 'UNPREPARE_SPELL', spellKey }),
    });
  }, [sheet, catalogueSpells, handleMutate]);

  // Each Text Field row saves on its own blur (TextFieldPanel), so this just forwards the
  // already-committed value to the server — no optimistic update, matching every other
  // "wait for the server's own response" mutation in this file.
  const handleUpdateBackgroundField = useCallback(
    (field: BackgroundField, value: string) => {
      setMutationError(null);
      postUpdateBackgroundField(character.id, field, value)
        .then(setSheet)
        .catch(() => setMutationError('Change failed to reach the server. Nothing was saved.'));
    },
    [character.id],
  );

  const handleOpenTextField = useCallback((request: TextFieldRequest) => setSidebarContent(request), []);

  const handleOpenConditions = useCallback(() => {
    if (!sheet) {
      return;
    }
    setSidebarContent({
      kind: 'conditions',
      title: 'Conditions',
      conditions: conditionEntries(sheet),
      onToggle: (key) => handleMutate({ type: 'TOGGLE_CONDITION', condition: key }),
      exhaustionLevel: sheet.exhaustionLevel,
      exhaustionIcon: <FrameIcon svg={conditionExhaustedSvg} aria-hidden />,
      exhaustionRulesText: EXHAUSTION_RULES_TEXT,
      onExhaustionChange: (level) => handleMutate({ type: 'SET_EXHAUSTION_LEVEL', level }),
      activeEffects: sheet.activeEffects,
      onEndEffect: (effectKey) => handleMutate({ type: 'END_ACTIVE_EFFECT', effectKey }),
    });
  }, [sheet, handleMutate]);

  const handleOpenManageCustomActions = useCallback(() => {
    if (!sheet) {
      return;
    }
    setSidebarContent({
      kind: 'manageCustomActions',
      customActions: sheet.customActions,
      onAdd: (action) => handleMutate({ type: 'ADD_CUSTOM_ACTION', action }),
      onRemove: (actionKey) => handleMutate({ type: 'REMOVE_CUSTOM_ACTION', actionKey }),
      onOpenAction: (action) =>
        setSidebarContent(
          customActionDetailRequest(action, (actionKey, updated) => handleMutate({ type: 'UPDATE_CUSTOM_ACTION', actionKey, action: updated })),
        ),
    });
  }, [sheet, handleMutate]);

  const handleOpenManageInventory = useCallback(() => {
    if (!sheet) {
      return;
    }
    setSidebarContent({
      kind: 'manageInventory',
      items: sheet.items,
      sections: sheet.encumbrance.sections,
      catalogue: catalogueItems,
      onAdd: (draft) => handleMutate({ type: 'ADD_ITEM', ...draft }),
      onAddFromCatalogue: (catalogueEntryId) => handleMutate({ type: 'ADD_CATALOGUE_ITEM', catalogueEntryId }),
      onRemove: (itemKey) => handleMutate({ type: 'REMOVE_ITEM', itemKey }),
      onMove: (itemKey, storageLocation) => handleMutate({ type: 'MOVE_ITEM', itemKey, storageLocation }),
      onToggleEquip: (itemKey) => handleMutate({ type: 'TOGGLE_ITEM_EQUIPPED', itemKey }),
      onOpenItem: (item) =>
        setSidebarContent(buildItemDetailRequest(item, customizationsOf(sheet).items[item.key], handleMutate, hasBagOfHoldingIn(sheet))),
    });
  }, [sheet, catalogueItems, handleMutate]);

  // knownFeats re-derived from live sheet state on every render (see
  // displayedSidebarContent below), same treatment spellManagement/manageInventory get.
  const handleOpenManageFeats = useCallback(() => {
    if (!sheet) {
      return;
    }
    setSidebarContent({
      kind: 'featManagement',
      knownFeats: sheet.featureTraits.filter((feature) => feature.category === 'FEAT'),
      catalogue: catalogueFeats,
      onLearn: (catalogueEntryId) => handleMutate({ type: 'LEARN_FEAT', catalogueEntryId }),
      onRemove: (featureKey) => handleMutate({ type: 'REMOVE_FEAT', featureKey }),
    });
  }, [sheet, catalogueFeats, handleMutate]);

  // The collection editor's sections and the log's entries are re-derived from
  // live state on every render (rather than trusted as the snapshot captured
  // when the panel was opened), so a proficiency edit or a new roll is
  // reflected immediately without extra bookkeeping in the handlers above. An
  // entity detail panel opts into the same treatment via `refreshActionBar`
  // (see EntityDetailRequest) — only Extras supplies one so far, since it is
  // the first entity detail trigger whose own action bar can change the
  // values it displays while the panel stays open.
  const displayedSidebarContent: SidebarContent | null =
    sidebarContent?.kind === 'pane' && sheet
      ? { ...sidebarContent, content: sidebarContent.render(sheet) }
      : sidebarContent?.kind === 'characterMenu' && sheet
      ? { ...sidebarContent, name: characterName, experience: sheet.experience ?? null }
      : sidebarContent?.kind === 'changeAppearance' && sheet
        ? { ...sidebarContent, current: sheet.sheetTheme ?? DEFAULT_SHEET_THEME }
      : sidebarContent?.kind === 'manageExperience' && sheet?.experience
        ? { ...sidebarContent, experience: sheet.experience }
      : sidebarContent?.kind === 'conditions' && sheet
        ? {
            ...sidebarContent,
            conditions: conditionEntries(sheet),
            exhaustionLevel: sheet.exhaustionLevel,
            activeEffects: sheet.activeEffects,
            onEndEffect: (effectKey: string) => handleMutate({ type: 'END_ACTIVE_EFFECT', effectKey }),
          }
        : sidebarContent?.kind === 'log'
          ? { ...sidebarContent, entries: rollHistory }
        : sidebarContent?.kind === 'entityDetail' && sidebarContent.refresh && sheet
          ? (sidebarContent.refresh(sheet) ?? sidebarContent)
        : sidebarContent?.kind === 'entityDetail' && sidebarContent.refreshActionBar && sheet
          ? { ...sidebarContent, actionBar: sidebarContent.refreshActionBar(sheet) }
          : sidebarContent?.kind === 'spellManagement' && sheet
            ? {
                ...sidebarContent,
                classes: sheet.spellcasting,
                knownSpells: sheet.spells,
                slots: sheet.spellSlots,
                // Rebuilt every render, not just at open time: each wraps `handleMutate`,
                // whose own optimistic-rollback snapshot is the `sheet` closed over here —
                // a stale wrapper from open time would roll a later failure back past
                // successful learns/prepares made in between.
                onLearn: (catalogueEntryId: string, className: string) =>
                  handleMutate({ type: 'LEARN_SPELL', catalogueEntryId, className }),
                onRemove: (spellKey: string) => handleMutate({ type: 'REMOVE_SPELL', spellKey }),
                onPrepare: (spellKey: string) => handleMutate({ type: 'PREPARE_SPELL', spellKey }),
                onUnprepare: (spellKey: string) => handleMutate({ type: 'UNPREPARE_SPELL', spellKey }),
              }
            : sidebarContent?.kind === 'hpManagement' && sheet
              ? {
                  ...sidebarContent,
                  ...hpManagementValues(sheet),
                }
              : sidebarContent?.kind === 'manageCustomActions' && sheet
                ? { ...sidebarContent, customActions: sheet.customActions }
                : sidebarContent?.kind === 'manageInventory' && sheet
                  ? { ...sidebarContent, items: sheet.items, sections: sheet.encumbrance.sections, catalogue: catalogueItems }
                  : sidebarContent?.kind === 'featManagement' && sheet
                    ? {
                        ...sidebarContent,
                        knownFeats: sheet.featureTraits.filter((feature) => feature.category === 'FEAT'),
                        // Rebuilt every render, same reasoning as spellManagement's own onLearn/onRemove above.
                        onLearn: (catalogueEntryId: string) => handleMutate({ type: 'LEARN_FEAT', catalogueEntryId }),
                        onRemove: (featureKey: string) => handleMutate({ type: 'REMOVE_FEAT', featureKey }),
                      }
                    : sidebarContent;

  if (error) {
    return (
      <div>
        <button type="button" onClick={onBack}>
          ← Back to characters
        </button>
        <p role="alert">{error}</p>
      </div>
    );
  }

  if (!sheet) {
    return null;
  }

  return (
    <LastingEffectSpellsContext.Provider value={lastingEffectSpells}>
    <SheetShell
      pageClassName="parchment-page"
      characterName={characterName}
      portrait={character.portrait}
      systemId={character.systemId}
      level={sheet.level}
      userLabel={userLabel}
      onOpenLog={handleOpenLog}
      onOpenShortRest={handleOpenShortRest}
      onOpenLongRest={handleOpenLongRest}
      shortRestIcon={<FrameIcon svg={shortRestIconSvg} className="rest-button__icon" />}
      longRestIcon={<FrameIcon svg={longRestIconSvg} className="rest-button__icon" />}
      gameLogIcon={<FrameIcon svg={gameLogIconSvg} className="rest-button__icon game-log" />}
      onOpenCharacter={handleOpenCharacterMenu}
      progress={sheet.experience?.advancement === 'XP' ? <XpBar experience={sheet.experience} variant="header" /> : undefined}
      portraitBadge={
        sheet.experience?.levelUpAvailable && (sidebarHidden || sidebarContent?.kind !== 'characterMenu') ? (
          <button type="button" className="level-up-arrow" title="Level up available" aria-label="Level up available" onClick={handleOpenCharacterMenu}>
            <svg viewBox="0 0 16 16" width="16" height="16" fill="currentColor" aria-hidden="true">
              <path d="M8 1 15 9h-4v6H5V9H1z" />
            </svg>
          </button>
        ) : undefined
      }
    >
      {mutationError && <p role="alert">{mutationError}</p>}
      <Dnd5eVitalsTopRow
        sheet={sheet}
        onRoll={handleRoll}
        onRollContextMenu={handleRollContextMenu}
        onMutate={handleMutate}
        onExplain={handleExplain}
        onOpenPane={handleOpenPane}
        onOpenHpManagement={handleOpenHpManagement}
        onRollDeathSave={handleRollDeathSave}
      />
      <div>
        <Dnd5eVitalsColumns
          sheet={sheet}
          onRoll={handleRoll}
          onRollContextMenu={handleRollContextMenu}
          onOpenPane={handleOpenPane}
          onOpenConditions={handleOpenConditions}
          onMutate={handleMutate}
          onOpenDetail={handleOpenDetail}
          onOpenSpellManagement={handleOpenSpellManagement}
          onOpenManageCustomActions={handleOpenManageCustomActions}
          onOpenManageInventory={handleOpenManageInventory}
          onOpenManageFeats={handleOpenManageFeats}
          backgroundSuggestions={backgroundSuggestions}
          onUpdateBackgroundField={handleUpdateBackgroundField}
          onOpenTextField={handleOpenTextField}
        />
      </div>
      <DiceTray latestRoll={sessionLatestRoll} error={rollError} />
      <CustomRollPicker onRoll={handleManualRoll} />
      <Sidebar content={displayedSidebarContent} hidden={sidebarHidden} onHide={hideSidebar} onShow={showSidebar} />
      {rollModeRequest && (
        <RollModeMenu
          request={rollModeRequest}
          onConfirm={(kind, key, advantage, disadvantage) => handleRoll(kind, key, undefined, { advantage, disadvantage })}
          onClose={() => setRollModeRequest(null)}
        />
      )}
    </SheetShell>
    </LastingEffectSpellsContext.Provider>
  );
}
