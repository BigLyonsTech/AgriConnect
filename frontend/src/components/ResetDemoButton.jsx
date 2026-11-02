import { useEffect, useState } from 'react';
import { resetDemo } from '../api/authApi';
import { useAuth } from '../features/auth/AuthContext';

const DEMO_DOMAIN = '@agriconnect.demo';

/**
 * Shown only to the demo users. Restores the demo data (including the PAID
 * order held in escrow) so the walkthrough can be run again. Two clicks:
 * the first arms it, the second resets; it disarms itself after 4 seconds.
 */
export default function ResetDemoButton() {
  const { user, loginAsDemo } = useAuth();
  const [armed, setArmed] = useState(false);
  const [busy, setBusy] = useState(false);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!armed) return undefined;
    const timer = setTimeout(() => setArmed(false), 4000);
    return () => clearTimeout(timer);
  }, [armed]);

  if (!user?.email?.endsWith(DEMO_DOMAIN)) return null;

  const handleClick = async () => {
    if (!armed) {
      setArmed(true);
      setFailed(false);
      return;
    }
    setBusy(true);
    try {
      await resetDemo();
      // Stay signed in as the same demo role, then reload so every page refetches.
      await loginAsDemo(user.role === 'BUYER' ? 'buyer' : 'farmer');
      window.location.reload();
    } catch {
      setFailed(true);
      setArmed(false);
      setBusy(false);
    }
  };

  let label = 'Reset demo';
  if (busy) label = 'Resetting…';
  else if (armed) label = 'Click to confirm';
  else if (failed) label = 'Reset failed, retry';

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={busy}
      className={'app-reset-demo' + (armed ? ' armed' : '')}
      title="Restore the demo data to its starting state"
    >
      {label}
    </button>
  );
}
