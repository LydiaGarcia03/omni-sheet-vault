import type { Portrait } from './portrait';
import { portraitImageUrl } from './portraitPresets';

type CharacterPortraitProps = {
  portrait: Portrait | null | undefined;
  systemId: string;
  /** The element's class: its size and frame come from the caller's stylesheet. */
  className: string;
  alt?: string;
};

/** A character's portrait image, or the caller's empty placeholder box when there is none. */
export function CharacterPortrait({ portrait, systemId, className, alt = '' }: CharacterPortraitProps) {
  const url = portraitImageUrl(portrait, systemId);
  if (!url) {
    return <div className={className} aria-hidden="true" />;
  }
  return <img className={`${className} has-image`} src={url} alt={alt} />;
}
