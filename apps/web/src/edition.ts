/** Which build this is: the desktop edition (`vite build --mode desktop`) has one local player and no accounts to manage. */
export const isDesktopEdition = import.meta.env.MODE === 'desktop';
