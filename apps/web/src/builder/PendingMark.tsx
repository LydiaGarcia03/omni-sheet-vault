/** The builder's "still to decide" marker: a red disc with an exclamation mark, placed beside what needs the decision. */
export function PendingMark() {
  return (
    <svg className="builder-pending-mark" viewBox="0 0 16 16" width="16" height="16" role="img" aria-label="Still to choose">
      <title>Still to choose</title>
      <circle cx="8" cy="8" r="8" fill="currentColor" />
      <rect x="7" y="3.5" width="2" height="6" rx="1" fill="#fff" />
      <circle cx="8" cy="12" r="1.2" fill="#fff" />
    </svg>
  );
}
