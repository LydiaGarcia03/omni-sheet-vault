import type { MechanicRequest } from './api';
import { RulesText } from './RulesText';
import { SidebarHeader } from './sidebarParts';

/**
 * The Mechanic mold's content (ui-design-system.md): the standard sidebar header, an introductory rules text, a
 * caller-built `body` (controls and confirm button, see `MechanicRequest`), then the closing rules text.
 */
export function MechanicPanel({ title, summary, mutedSummary = false, body, rulesText = '' }: MechanicRequest) {
  return (
    <div className={mutedSummary ? 'mechanic-panel mechanic-panel--muted-intro' : 'mechanic-panel'}>
      <SidebarHeader title={title} />
      <RulesText text={summary} placement="intro" />
      {body}
      <RulesText text={rulesText} />
    </div>
  );
}
