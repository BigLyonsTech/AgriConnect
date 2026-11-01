import { useState } from 'react';

// index.html sets data-theme before first paint (saved choice, else the OS
// setting); this button just flips it and remembers the choice.
export default function ThemeToggle({ className = '' }) {
  const [theme, setTheme] = useState(() => document.documentElement.dataset.theme || 'light');

  function toggle() {
    const next = theme === 'dark' ? 'light' : 'dark';
    document.documentElement.dataset.theme = next;
    try {
      localStorage.setItem('theme', next);
    } catch {
      // Storage blocked (private mode etc.) - the theme still applies for this visit.
    }
    setTheme(next);
  }

  const label = theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode';
  return (
    <button type="button" className={'theme-toggle ' + className} onClick={toggle} aria-label={label} title={label}>
      {theme === 'dark' ? '☀️' : '🌙'}
    </button>
  );
}
