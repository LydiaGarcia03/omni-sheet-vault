import { useState } from 'react';
import type { RollContextMenuHandler, RollHandler } from '../../dice/api';
import type {
  AttackRow as AttackRowData,
  CharacterSheet,
  CustomAction,
  EntityDetailHandler,
  EntityDetailRequest,
  FeatureAction,
  MutationHandler,
  Spell,
} from '../../sheet/api';
import { FilterChips } from '../../sheet/FilterChips';
import { GridHeaderRow, GridScroll } from '../../sheet/ListGrid';
import { ListRow } from '../../sheet/ListRow';
import { ActionsInCombatList } from './ActionsInCombatList';
import { AttackRow } from './AttackRow';
import { customizationsOf } from './customizations';
import { buildItemDetailRequest, hasBagOfHoldingIn } from './ItemRow';
import { FeatureActionRow } from './FeatureActionRow';
import { customActionDetailRequest } from './customActionDetail';
import { featureActionDetailRequest } from './featureDetail';
import { levelAbbreviation } from './SpellCast';
import { buildSpellDetailRequest } from './spellDetail';
import { isCombatSpell, spellActionType } from './spellCombat';
import { SpellAttackRow } from './SpellAttackRow';
import { BONUS_ACTIONS, OTHER_ACTIONS, REACTIONS, STANDARD_ACTIONS, type StandardAction } from './standardActions';

const CHIPS = [
  { id: 'all', label: 'All' },
  { id: 'attack', label: 'Attack' },
  { id: 'ACTION', label: 'Action' },
  { id: 'BONUS_ACTION', label: 'Bonus Action' },
  { id: 'REACTION', label: 'Reaction' },
  { id: 'OTHER', label: 'Other' },
  { id: 'limited-use', label: 'Limited Use' },
];

type ActionsTabProps = {
  sheet: CharacterSheet;
  onRoll: RollHandler;
  onRollContextMenu: RollContextMenuHandler;
  onMutate: MutationHandler;
  onOpenDetail: EntityDetailHandler;
  onOpenManageCustomActions: () => void;
};

function formatSigned(amount: number): string {
  return amount >= 0 ? `+${amount}` : `−${Math.abs(amount)}`;
}

/**
 * A player-authored custom action not shown in the Attack table — see
 * `ManageCustomActionsPanel`'s doc comment for why a `displayAsAttack` one that
 * resolves to a to-hit roll renders there instead. No box track: unlike a
 * feature action, a custom action is not a limited-use resource.
 */
function CustomActionRow({ action, onOpenDetail }: { action: CustomAction; onOpenDetail: () => void }) {
  return (
    <ListRow className="action-feature reveal" ariaLabel={`Open ${action.name} details`} onOpen={onOpenDetail}>
      <div className="action-feature__body">
        <h3 className="reveal actions-tab__heading">{action.name}</h3>
        <p className="action-feature__description">{action.description}</p>
      </div>
    </ListRow>
  );
}

type SectionHeadingProps = {
  title: string;
  detail?: string;
  onManageCustom: () => void;
};

/**
 * DOM-measured against D&D Beyond's own section headings (2026-09-03):
 * every action-type section — not just Attacks — gets this same treatment
 * (accent-colored uppercase title, an optional plain-case detail run, a
 * "Manage Custom" link, a divider directly beneath). Confirmed live against
 * a second reference character specifically to see "BONUS ACTIONS"/
 * "REACTIONS"/"OTHER" headings, which Aria's own build never renders.
 */
function SectionHeading({ title, detail, onManageCustom }: SectionHeadingProps) {
  return (
    <div className="actions-tab__section-heading">
      <span>
        {title}
        {detail && <span className="actions-tab__heading-detail"> • {detail}</span>}
      </span>
      <button type="button" className="actions-tab__manage-custom" onClick={onManageCustom}>
        Manage Custom
      </button>
    </div>
  );
}

type SpellSummaryListProps = {
  title: string;
  spells: Spell[];
  sheet: CharacterSheet;
  onMutate: MutationHandler;
  onRoll: RollHandler;
  onOpenDetail: EntityDetailHandler;
};

/**
 * "Spells"/"Ritual Spells" — a compact, comma-separated line per action-type
 * section, matching D&D Beyond's own `.ct-feature-snippet__spells`/`--ritual`
 * treatment: a plain flowing paragraph — no left-border box, unlike
 * `.actions-tab__standard-list` above — each entry 12px/400/italic
 * ("Name(Level)"), comma-separated, opening the spell's own Entity Detail on
 * click. The section heading reuses the existing `.actions-tab__heading`.
 * D&D Beyond also appends " • <source book>" per entry — this app has no
 * source field on `Spell` yet (same gap Features & Traits' own class-group
 * heading doc comment states), so that suffix is omitted rather than
 * invented. "Spells" lists every non-attack spell in the bucket — a combat
 * spell already has its own full row in the Attack table above and never
 * also appears here. "Ritual Spells" lists every ritual-tagged spell in the
 * bucket independently, so a spell can appear in both lines at once (e.g.
 * one with a 1-minute casting time that's also castable as a ritual).
 */
function SpellSummaryList({ title, spells, sheet, onMutate, onRoll, onOpenDetail }: SpellSummaryListProps) {
  if (spells.length === 0) {
    return null;
  }
  return (
    <div>
      <h3 className="actions-tab__heading">{title}</h3>
      <p className="actions-tab__spell-list">
        {spells.map((spell, index) => (
          <span key={spell.key}>
            <button
              type="button"
              className="reveal"
              onClick={() => onOpenDetail(buildSpellDetailRequest(spell, sheet, onMutate, onRoll))}
            >
              {spell.name} ({levelAbbreviation(spell.level)})
            </button>
            {index < spells.length - 1 ? ', ' : ''}
          </span>
        ))}
      </p>
    </div>
  );
}

const ATTACK_TABLE_COLUMNS = [
  { key: 'icon', className: 'action-row__icon' },
  { key: 'name', className: 'action-row__name grid-header-cell', content: 'Attack' },
  { key: 'range', className: 'action-row__range grid-header-cell', content: 'Range' },
  {
    key: 'hit-dc',
    className: 'action-row__value-cell action-row__value-cell--prominent',
    content: <div className="action-row__value action-row__value--prominent grid-header-cell">Hit / DC</div>,
  },
  {
    key: 'damage',
    className: 'action-row__value-cell action-row__value-cell--damage',
    content: <div className="action-row__value action-row__value--damage grid-header-cell">Damage</div>,
  },
  { key: 'notes', className: 'action-row__notes grid-header-cell', content: 'Notes' },
];

type SectionConfig = {
  bucket: string;
  title: string;
  standardActions: StandardAction[];
};

const SECTIONS: SectionConfig[] = [
  { bucket: 'ACTION', title: 'Actions', standardActions: STANDARD_ACTIONS },
  { bucket: 'BONUS_ACTION', title: 'Bonus Actions', standardActions: BONUS_ACTIONS },
  { bucket: 'REACTION', title: 'Reactions', standardActions: REACTIONS },
  { bucket: 'OTHER', title: 'Other', standardActions: OTHER_ACTIONS },
];

/**
 * Build order step 6 (systems/dnd-5e/sheet-build.md): the tab bar's first tab, proving the
 * pattern. Sections render in systems/dnd-5e/sheet-ui.md's own fixed order — attacks, then
 * the standard combat actions, then class features — and the filter chips narrow
 * that same ordered list rather than toggling independent sections. "Limited use"
 * matches by track presence instead of action type, so it cuts across the other
 * chips' categories.
 *
 * **Restructured 2026-09-03, direct owner report ("vi que não tem também a
 * divisão de sub-seções"):** every action type (Action, Bonus Action, Reaction,
 * Other) gets its own headed section — each with its own "Actions in Combat"
 * universal list and its own bucket of that type's named features.
 *
 * Each section renders its own attack table, gated by `AttackRow.actionType`
 * (backend field, `Dnd5eSheetCalculator.attacks()`) — a catalog weapon or
 * equipped item is always `ACTION` (neither has an activation-type source),
 * but a `displayAsAttack` custom action can carry any of the four, matching
 * how D&D Beyond buckets its own attack table by action type too. The
 * "Attack" chip shows every bucket's own table (`showAttackTableFor`); the
 * four action-type chips show only their own bucket's table.
 *
 * Each section also renders a "Spells"/"Ritual Spells" summary line — see
 * `SpellSummaryList`'s own doc comment — bucketed via `spellActionType`
 * (`spellCombat.ts`), reading `Spell.castingTime`.
 *
 * Feature action box tracks are real mutations as of phase 9
 * (`FeatureActionRow.tsx`, `Dnd5eSheetMutator.useFeatureAction`/
 * `restoreFeatureAction`). The sidebar copy of the same track supplies
 * `refreshActionBar` so a spend/restore click updates it live, matching the
 * live-refresh treatment `ExtraRow.tsx` established.
 *
 * Within each section's own attack table, `sheet.attacks` splits into
 * `weaponEntries`/`unarmedEntries` by a `category` keyword match (same
 * convention `AttackRow.tsx`'s own `attackIcon()` uses), rendered
 * weapons-then-spells-then-unarmed — an always-available attack like
 * Unarmed Strike sorts last.
 */
/** The derived Unarmed Strike (key `unarmed-strike`, shown as a Melee Attack like D&D Beyond), or a hand-authored unarmed attack. */
function isUnarmed(key: string, attack: AttackRowData): boolean {
  return key === 'unarmed-strike' || attack.category.toLowerCase().includes('unarmed');
}

export function ActionsTab({ sheet, onRoll, onRollContextMenu, onMutate, onOpenDetail, onOpenManageCustomActions }: ActionsTabProps) {
  const [activeChip, setActiveChip] = useState('all');

  const matchesChip = (category: string, hasTrack: boolean): boolean => {
    if (activeChip === 'all') {
      return true;
    }
    if (activeChip === 'limited-use') {
      return hasTrack;
    }
    return category === activeChip;
  };

  const featuresOfType = (type: string) =>
    sheet.featureActions.filter((feature) => feature.actionType === type && matchesChip(feature.actionType, feature.maxUses != null));

  // A displayAsAttack custom action with a stat set resolves to a to-hit roll and
  // renders in the Attack table instead (folded server-side into `sheet.attacks`,
  // carrying its own real actionType through — see this file's own doc comment
  // and ManageCustomActionsPanel's).
  const shownAsAttack = (action: CustomAction) => action.displayAsAttack && action.stat != null && action.activationType != null;
  const actionTypeBucket = (activationType: string | null) =>
    activationType == null
      ? null
      : activationType === 'ACTION' || activationType === 'BONUS_ACTION' || activationType === 'REACTION'
        ? activationType
        : 'OTHER';
  const customActionsOfType = (type: string) =>
    sheet.customActions.filter((action) => !shownAsAttack(action) && actionTypeBucket(action.activationType) === type && matchesChip(type, false));

  const allAttackEntries = Object.entries(sheet.attacks);
  const attackEntriesForBucket = (bucket: string) => allAttackEntries.filter(([, attack]) => attack.actionType === bucket);
  const weaponEntriesFor = (bucket: string) => attackEntriesForBucket(bucket).filter(([key, attack]) => !isUnarmed(key, attack));
  const unarmedEntriesFor = (bucket: string) => attackEntriesForBucket(bucket).filter(([key, attack]) => isUnarmed(key, attack));

  const combatSpells = sheet.spells.filter(isCombatSpell);
  const combatSpellsFor = (bucket: string) => combatSpells.filter((spell) => spellActionType(spell.castingTime) === bucket);
  const spellSummaryFor = (bucket: string) =>
    sheet.spells.filter((spell) => !isCombatSpell(spell) && spellActionType(spell.castingTime) === bucket);
  const ritualSpellSummaryFor = (bucket: string) => sheet.spells.filter((spell) => spell.ritual && spellActionType(spell.castingTime) === bucket);

  const showAttackTableFor = (bucket: string, weaponEntries: unknown[], unarmedEntries: unknown[], spells: Spell[]) =>
    (activeChip === 'all' || activeChip === 'attack' || activeChip === bucket) &&
    (weaponEntries.length > 0 || unarmedEntries.length > 0 || spells.length > 0);

  const renderAttackRow = ([key, attack]: [string, AttackRowData]) => (
    <AttackRow
      key={key}
      attack={attack}
      unarmed={isUnarmed(key, attack)}
      onRollHit={() => onRoll('ATTACK_HIT', key)}
      onRollHitContextMenu={(x, y) => onRollContextMenu(x, y, 'ATTACK_HIT', key, formatSigned(attack.toHit.value))}
      onRollDamage={() => onRoll('ATTACK_DAMAGE', key)}
      onRollVersatileDamage={() => onRoll('ATTACK_DAMAGE_VERSATILE', key)}
      onOpenDetail={() => onOpenDetail(attackDetailRequest(key, attack))}
    />
  );

  const attackDetailRequest = (key: string, attack: AttackRowData): EntityDetailRequest => {
    const item = sheet.items.find((candidate) => candidate.key === key);
    if (item) {
      return buildItemDetailRequest(item, customizationsOf(sheet).items[key], onMutate, hasBagOfHoldingIn(sheet));
    }
    return {
      kind: 'entityDetail',
      name: attack.name,
      metadata: [
        { label: 'Category', value: attack.category },
        { label: 'Range', value: attack.range },
        { label: 'Damage Type', value: attack.damageType },
      ],
      description: attack.notes,
    };
  };

  const renderAttackTable = (weaponEntries: [string, AttackRowData][], unarmedEntries: [string, AttackRowData][], spells: Spell[]) => (
    <>
      <GridHeaderRow rowClassName="action-row" columns={ATTACK_TABLE_COLUMNS} />
      <div className="action-list">
        {weaponEntries.map(renderAttackRow)}
        {spells.map((spell) => (
          <SpellAttackRow
            key={spell.key}
            spell={spell}
            sheet={sheet}
            onRoll={onRoll}
            onRollContextMenu={onRollContextMenu}
            onMutate={onMutate}
            onOpenDetail={onOpenDetail}
          />
        ))}
        {unarmedEntries.map(renderAttackRow)}
      </div>
    </>
  );

  const featureDetailRequest = (feature: FeatureAction) => featureActionDetailRequest(feature, sheet, onMutate, onOpenDetail);

  const openCustomAction = (action: CustomAction) =>
    onOpenDetail(
      customActionDetailRequest(action, (actionKey, updated) => onMutate({ type: 'UPDATE_CUSTOM_ACTION', actionKey, action: updated })),
    );

  const renderSection = ({ bucket, title, standardActions }: SectionConfig) => {
    const detail = bucket === 'ACTION' ? `Attacks per Action: ${sheet.attacksPerAction}` : undefined;
    const weaponEntries = weaponEntriesFor(bucket);
    const unarmedEntries = unarmedEntriesFor(bucket);
    const bucketCombatSpells = combatSpellsFor(bucket);
    const showAttackTable = showAttackTableFor(bucket, weaponEntries, unarmedEntries, bucketCombatSpells);
    const showInCombat = activeChip === 'all' || activeChip === bucket;
    const bucketFeatures = featuresOfType(bucket);
    const bucketCustomActions = customActionsOfType(bucket);
    const spellSummary = showInCombat ? spellSummaryFor(bucket) : [];
    const ritualSpellSummary = showInCombat ? ritualSpellSummaryFor(bucket) : [];
    const showSection = showAttackTable || showInCombat || bucketFeatures.length > 0 || bucketCustomActions.length > 0;

    if (!showSection) {
      return null;
    }

    return (
      <section key={bucket}>
        <SectionHeading title={title} detail={detail} onManageCustom={onOpenManageCustomActions} />
        {showAttackTable && renderAttackTable(weaponEntries, unarmedEntries, bucketCombatSpells)}
        {showInCombat && <ActionsInCombatList actions={standardActions} onOpenDetail={onOpenDetail} />}
        <SpellSummaryList title="Spells" spells={spellSummary} sheet={sheet} onMutate={onMutate} onRoll={onRoll} onOpenDetail={onOpenDetail} />
        <SpellSummaryList title="Ritual Spells" spells={ritualSpellSummary} sheet={sheet} onMutate={onMutate} onRoll={onRoll} onOpenDetail={onOpenDetail} />
        {(bucketFeatures.length > 0 || bucketCustomActions.length > 0) && (
          <div className="action-list">
            {bucketFeatures.map((feature) => (
              <FeatureActionRow key={feature.key} feature={feature} onMutate={onMutate} onOpenDetail={() => onOpenDetail(featureDetailRequest(feature))} />
            ))}
            {bucketCustomActions.map((action) => (
              <CustomActionRow key={action.key} action={action} onOpenDetail={() => openCustomAction(action)} />
            ))}
          </div>
        )}
      </section>
    );
  };

  return (
    <div className="actions-tab">
      <FilterChips chips={CHIPS} activeChip={activeChip} onSelect={setActiveChip} />

      <GridScroll>{SECTIONS.map(renderSection)}</GridScroll>
    </div>
  );
}
