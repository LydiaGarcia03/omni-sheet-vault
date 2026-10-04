import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { Spell } from '../../sheet/api';
import { LastingEffectSpellsContext } from './lastingEffectSpells';
import { SpellCast } from './SpellCast';

const mageArmor = {
  key: 'mage-armor',
  name: 'Mage Armor',
  level: 1,
  attackRoll: false,
  damageDiceCount: null,
  damageDiceSides: null,
} as unknown as Spell;

function renderCast(lastingEffectSpells: ReadonlySet<string>) {
  const onMutate = vi.fn();
  render(
    <LastingEffectSpellsContext.Provider value={lastingEffectSpells}>
      <SpellCast spell={mageArmor} spellSlots={[{ level: 1, maxSlots: 4, usedSlots: 0 }]} onMutate={onMutate} onRoll={vi.fn()} />
    </LastingEffectSpellsContext.Provider>,
  );
  return onMutate;
}

describe('SpellCast pools', () => {
  it('steps from the regular slots to the Pact Magic level, where the button spends a pact slot', () => {
    const onMutate = vi.fn();
    render(
      <SpellCast
        spell={mageArmor}
        spellSlots={[
          { level: 1, maxSlots: 4, usedSlots: 0 },
          { level: 2, maxSlots: 2, usedSlots: 1, pact: true },
        ]}
        onMutate={onMutate}
        onRoll={vi.fn()}
      />,
    );

    expect(screen.getByText('Spell Slot')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Increase cast level' }));
    fireEvent.click(screen.getByRole('button', { name: 'Cast Mage Armor using a 2nd Level Pact Magic slot' }));

    expect(screen.queryByText('Spell Slot')).toBeNull();
    expect(onMutate).toHaveBeenCalledWith({ type: 'CAST_SPELL', spellKey: 'mage-armor', slotLevel: 2, onSelf: true, pact: true });
  });

  it('shows only the Pact Slot button, at the pool’s level, for a pact caster', () => {
    render(<SpellCast spell={mageArmor} spellSlots={[{ level: 2, maxSlots: 2, usedSlots: 0, pact: true }]} onMutate={vi.fn()} onRoll={vi.fn()} />);

    expect(screen.queryByText('Spell Slot')).toBeNull();
    expect(screen.getByText('Pact Slot')).toBeInTheDocument();
    expect(screen.getByText('2nd')).toBeInTheDocument();
  });
});

describe('SpellCast without a slot', () => {
  const burningHands = {
    ...mageArmor,
    key: 'burning-hands',
    name: 'Burning Hands',
    usage: { mode: 'LIMITED', maxUses: 1, usedUses: 0, recharge: 'LONG_REST', castLevel: 2, selfOnly: false },
  } as unknown as Spell;

  it('uses a limited spell at its fixed level, without spending a slot', () => {
    const onMutate = vi.fn();
    render(<SpellCast spell={burningHands} spellSlots={[{ level: 1, maxSlots: 4, usedSlots: 0 }]} onMutate={onMutate} onRoll={vi.fn()} />);

    fireEvent.click(screen.getByRole('button', { name: 'Use Burning Hands, 1 of 1 left' }));

    expect(screen.getByText('2nd')).toBeInTheDocument();
    expect(screen.getByText('Once per Long Rest')).toBeInTheDocument();
    expect(screen.queryByText('Spell Slot')).toBeNull();
    expect(screen.queryByRole('button', { name: 'Increase cast level' })).toBeNull();
    expect(onMutate).toHaveBeenCalledWith({ type: 'CAST_SPELL', spellKey: 'burning-hands', slotLevel: 0, onSelf: true, pact: false });
  });

  it('disables Use once its uses are spent', () => {
    const spent = { ...burningHands, usage: { ...burningHands.usage!, usedUses: 1 } } as Spell;
    render(<SpellCast spell={spent} spellSlots={[]} onMutate={vi.fn()} onRoll={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'Use Burning Hands, 0 of 1 left' })).toBeDisabled();
  });

  it('casts a self-only at-will spell on the character, with no target choice', () => {
    const armorOfShadows = {
      ...mageArmor,
      usage: { mode: 'AT_WILL', maxUses: 0, usedUses: 0, recharge: null, castLevel: null, selfOnly: true },
    } as unknown as Spell;
    const onMutate = vi.fn();
    render(
      <LastingEffectSpellsContext.Provider value={new Set(['mage-armor'])}>
        <SpellCast spell={armorOfShadows} spellSlots={[]} onMutate={onMutate} onRoll={vi.fn()} />
      </LastingEffectSpellsContext.Provider>,
    );

    fireEvent.click(screen.getByRole('button', { name: 'Cast Mage Armor at will' }));

    expect(screen.queryByRole('button', { name: 'Cast on an ally' })).toBeNull();
    expect(onMutate).toHaveBeenCalledWith({ type: 'CAST_SPELL', spellKey: 'mage-armor', slotLevel: 0, onSelf: true, pact: false });
  });
});

describe('SpellCast target', () => {
  it('offers no target choice for a spell without a lasting effect', () => {
    renderCast(new Set());

    expect(screen.queryByRole('button', { name: 'Cast on an ally' })).toBeNull();
  });

  it('casts on the character by default', () => {
    const onMutate = renderCast(new Set(['mage-armor']));

    fireEvent.click(screen.getByRole('button', { name: /Cast Mage Armor/ }));

    expect(onMutate).toHaveBeenCalledWith({ type: 'CAST_SPELL', spellKey: 'mage-armor', slotLevel: 1, onSelf: true, pact: false });
  });

  it('casts on an ally once Ally is picked', () => {
    const onMutate = renderCast(new Set(['mage-armor']));

    fireEvent.click(screen.getByRole('button', { name: 'Cast on an ally' }));
    fireEvent.click(screen.getByRole('button', { name: /Cast Mage Armor/ }));

    expect(screen.getByRole('button', { name: 'Cast on an ally' })).toHaveAttribute('aria-pressed', 'true');
    expect(onMutate).toHaveBeenCalledWith({ type: 'CAST_SPELL', spellKey: 'mage-armor', slotLevel: 1, onSelf: false, pact: false });
  });
});
