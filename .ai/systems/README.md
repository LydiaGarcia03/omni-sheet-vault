# Game systems

One folder per game system, named after its identifier in code. Everything
that belongs to one system lives here; platform documents live in
`../features/`.

```
systems/<system-id>/
├── *.md           # system-wide specs: rules, sheet build, sheet UI
├── features/      # one feature's plan or spec, for this system only
└── references/    # walkthroughs and audits against an external source
```

## Supported and planned systems

| System | Id | Status | Code | Start with |
| --- | --- | --- | --- | --- |
| Dungeons & Dragons 5e (2014) | `dnd-5e` | Playable: sheet, builder, level up | `ruleset.dnd5e`, `apps/web/src/systems/dnd5e` | `dnd-5e/sheet-build.md`, then `dnd-5e/sheet-ui.md` |
| Vampire: The Masquerade 5e | `vtm-v5` | Planned (roadmap phase 12) | — | `vtm-v5/features/implementation-plan.md`, then `vtm-v5/character-rules.md` |

## D&D 5e — `dnd-5e/`

| Document | What it is |
| --- | --- |
| `sheet-build.md` | The sheet's component inventory and build status |
| `sheet-ui.md` | Sheet layout and behaviour, the visual target, and how to verify against D&D Beyond |
| `features/character-creation.md` | Phase 11 spec: rules engine, data audit, creation flow (stages A–D) |
| `features/builder-refinements.md` | Builder refinements, round 1 |
| `features/5etools-ingestion.md` | 5etools → `content/dnd-5e/` converter |
| `features/inventory-equipment-mechanics.md` | Items, equipment and magic-item mechanics |
| `features/leveling-and-appearance.md` | XP, level up, sheet themes, death saves |
| `features/spellcasting-pools.md` | Pact Magic and slotless casting |
| `features/sidebar-fidelity-and-customization.md` | Sidebar fidelity and customize plan |
| `features/rules-text-slots.md` | Where the hand-written sidebar rules text goes |
| `references/dndbeyond-builder-walkthrough.md` | D&D Beyond builder walkthrough |
| `references/sheet-fidelity-audit.md` | Visual audit against D&D Beyond |
| `references/data-fidelity-audit.md` | Data audit against 5etools |

Related decisions: `../decisions/adr-0006-5etools-as-content-source.md`,
`../decisions/adr-0007-curated-modifier-overlay.md`.

## VtM 5e — `vtm-v5/`

| Document | What it is |
| --- | --- |
| `character-rules.md` | Rules and character options compiled from the core, Camarilla and Anarch books |
| `features/implementation-plan.md` | Demiplane walkthrough, seams to generalize, slices V0–V7, owner questions |

## Old paths (before 2026-10-03)

Older `changelog.md` entries use these names:

| Old | New |
| --- | --- |
| `rulesets/dnd-5e-sheet-build.md` | `systems/dnd-5e/sheet-build.md` |
| `rulesets/dnd-5e-sheet-ui.md` | `systems/dnd-5e/sheet-ui.md` |
| `rulesets/dnd-5e-sheet-fidelity-audit.md` | `systems/dnd-5e/references/sheet-fidelity-audit.md` |
| `rulesets/dnd-5e-data-fidelity-audit.md` | `systems/dnd-5e/references/data-fidelity-audit.md` |
| `features/dndbeyond-builder-walkthrough.md` | `systems/dnd-5e/references/dndbeyond-builder-walkthrough.md` |
| `features/{5etools-ingestion, builder-refinements, character-creation, inventory-equipment-mechanics, leveling-and-appearance, rules-text-slots, sidebar-fidelity-and-customization, spellcasting-pools}.md` | `systems/dnd-5e/features/<same name>.md` |
| `rulesets/vtm-v5-character-rules.md` | `systems/vtm-v5/character-rules.md` |
| `features/vampire-v5.md` | `systems/vtm-v5/features/implementation-plan.md` |
