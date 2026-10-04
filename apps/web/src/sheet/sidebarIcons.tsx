/** D&D Beyond's sidebar icons (`svg-index.html`: SidebarLeftSvg, SidebarRightSvg, LockSvg, UnlockSvg, PencilSvg). */

export function SidebarLeftIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" aria-hidden="true">
      <path
        fill="currentColor"
        d="M11,2.48,5,8l6,5.52a1.3,1.3,0,0,1-.21,2.12h0a2.25,2.25,0,0,1-2.68-.17L0,8,8.11.53A2.25,2.25,0,0,1,10.79.36h0A1.3,1.3,0,0,1,11,2.48Z"
      />
      <polygon fill="currentColor" points="6.92 8 16 0 16 16 6.92 8" />
    </svg>
  );
}

export function SidebarRightIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" aria-hidden="true">
      <path
        fill="currentColor"
        d="M5.21.36h0A2.25,2.25,0,0,1,7.89.53L16,8,7.89,15.47a2.25,2.25,0,0,1-2.68.17h0A1.3,1.3,0,0,1,5,13.52L11,8,5,2.48A1.3,1.3,0,0,1,5.21.36Z"
      />
      <polygon fill="currentColor" points="9.09 8 0 0 0 16 9.09 8" />
    </svg>
  );
}

export function LockIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10.2299 15.0756" aria-hidden="true">
      <path
        fill="currentColor"
        d="M9.30491,6.41174V4.19A4.19,4.19,0,0,0,.925,4.19V6.41174L0,6.54333v7.61309l5.115.91918,5.115-.91918V6.54333ZM2.30862,4.19a2.80632,2.80632,0,0,1,5.61264,0V6.21491L5.115,5.81568l-2.80633.39923Z"
      />
    </svg>
  );
}

export function UnlockIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10.23313 16" aria-hidden="true">
      <path
        fill="currentColor"
        d="M5.11656,6.73717l-2.80744.39938V4.19033a2.8071,2.8071,0,0,1,5.6142,0H9.30741a4.19119,4.19119,0,0,0-8.38238,0V7.33344L0,7.465v7.61549L5.11656,16l5.11657-.91947V7.465Z"
      />
    </svg>
  );
}

/** D&D Beyond's PencilSvg, beside a name that can be renamed. */
export function PencilIcon({ className }: { className?: string }) {
  return (
    <svg className={className} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 15 14" aria-hidden="true">
      <path
        fillRule="evenodd"
        clipRule="evenodd"
        fill="currentColor"
        d="M0.828181 12.0217C0.625019 13.125 1.12507 13.75 2.33489 13.5284L5.177 12.8479C5.67077 12.7297 6.12217 12.4771 6.48119 12.118L13.4476 5.15165C14.3262 4.27297 14.3262 2.84835 13.4476 1.96967L12.3869 0.90901C11.5082 0.0303294 10.0836 0.0303308 9.20493 0.90901L2.23855 7.87539C1.87953 8.23441 1.62691 8.68581 1.50869 9.17959L0.828181 12.0217ZM12.3869 4.09099L11.3852 5.09272L9.26385 2.9714L10.2656 1.96967C10.5585 1.67678 11.0334 1.67678 11.3262 1.96967L12.3869 3.03033C12.6798 3.32322 12.6798 3.7981 12.3869 4.09099ZM4.56587 11.4518L2.38184 11.9748L2.90477 9.79074C2.94584 9.61921 3.03155 9.4616 3.15319 9.33389L8.20322 4.03206L10.3245 6.15338L5.02271 11.2034C4.895 11.3251 4.73739 11.4108 4.56587 11.4518Z"
      />
    </svg>
  );
}

/** The pane's top edge (D&D Beyond's sidebar border, viewBox 340×18); the bottom edge is the same drawing rotated. */
export function SidebarBorder({ bottom = false }: { bottom?: boolean }) {
  return (
    <svg
      className={bottom ? 'sidebar__border sidebar__border--bottom' : 'sidebar__border'}
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 340 18"
      aria-hidden="true"
    >
      <path className="sidebar__border-bg" d="M328.1,6.19H201a57,57,0,0,1-30.15,9.14A54.55,54.55,0,0,1,141,6.19H12A30.25,30.25,0,0,1,.4,17.26V18H340v-.27A28.68,28.68,0,0,1,328.1,6.19Z" />
      <path className="sidebar__border-stroke" strokeWidth="2" d="M339.6,17.09C328.4,12.16,325.17,1,325.17,1H169.42" />
      <path className="sidebar__border-stroke" strokeWidth="2" d="M.4,17.09C11.6,12.16,14.83,1,14.83,1H170.41" />
      <path className="sidebar__border-stroke" strokeWidth="2" d="M200.23,6.19c-7.16,6.12-20.56,8.32-30.35,8.32" />
      <path className="sidebar__border-stroke" strokeWidth="2" d="M170.12,14.51c-9.79,0-23.19-2.2-30.35-8.32" />
      <path className="sidebar__border-fill" d="M167.12,14.51a3,3,0,1,0,3-3,3,3,0,0,0-3,3" />
      <path className="sidebar__border-stroke" strokeWidth="3" d="M1.5,18V10.89A5.44,5.44,0,0,1,7,5.5h326.1a5.44,5.44,0,0,1,5.45,5.39V18" />
    </svg>
  );
}
