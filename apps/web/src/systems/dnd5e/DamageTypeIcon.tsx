import { FrameIcon } from './FrameIcon';

type DamageTypeIconVariant = 'resistance' | 'immunity' | 'vulnerability';

type DamageTypeIconProps = {
  svg: string;
  variant: DamageTypeIconVariant;
  label: string;
};

/**
 * D&D Beyond's own shield icon (ImmunitySvg/ResistanceSvg/VulnerabilitySvg),
 * used as-is rather than an original shape. Resistance and Immunity share
 * the confirmed `--defense-badge` green; Vulnerability uses
 * `--status-negative` instead — a downside rather than a protection, and
 * unconfirmed live (this app's reference character has neither).
 */
export function DamageTypeIcon({ svg, variant, label }: DamageTypeIconProps) {
  return <FrameIcon svg={svg} className={`damage-type-icon damage-type-icon--${variant}`} title={label} aria-hidden />;
}
