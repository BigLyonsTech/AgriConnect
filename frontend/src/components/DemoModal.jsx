import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import TryDemoButtons from './TryDemoButtons';
import '../styles/demo.css';

// Screens captured from the live demo org (see public/demo/README.md).
const TOUR_STEPS = [
  { src: '/demo/tour-dashboard.webp', title: 'Your farm business at a glance',
    text: 'Farms, crops, inventory, revenue, expenses and net profit, calculated live for your organisation.' },
  { src: '/demo/tour-farms.webp', title: 'Farms with live weather',
    text: 'Register each farm and check the current weather for its location, powered by OpenWeatherMap.' },
  { src: '/demo/tour-crops.webp', title: 'Track crops and livestock',
    text: 'Planting and harvest dates, crop status, herd sizes and health notes, per farm.' },
  { src: '/demo/tour-inventory.webp', title: 'Inventory in kilograms',
    text: 'Stock levels for every farm, from seed and fertiliser to harvested produce ready to sell.' },
  { src: '/demo/tour-finance.webp', title: 'Expenses, revenue and profit',
    text: 'Record money in and out per farm. Profit updates as you type.' },
  { src: '/demo/tour-marketplace.webp', title: 'Buyers order across organisations',
    text: 'Listings are visible to every buyer on the platform. Buyers order straight from the listing.' },
  { src: '/demo/tour-orders.webp', title: 'Escrow-protected payments',
    text: 'Buyers pay through Paystack into escrow. The seller fulfils the order, then releases the funds.' },
];

export default function DemoModal({ open, onClose }) {
  const [tab, setTab] = useState('video');
  const [step, setStep] = useState(0);

  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => {
      if (e.key === 'Escape') onClose();
      if (tab === 'tour' && e.key === 'ArrowRight') setStep((s) => Math.min(s + 1, TOUR_STEPS.length - 1));
      if (tab === 'tour' && e.key === 'ArrowLeft') setStep((s) => Math.max(s - 1, 0));
    };
    document.addEventListener('keydown', onKey);
    const overflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = overflow;
    };
  }, [open, tab, onClose]);

  if (!open) return null;
  const current = TOUR_STEPS[step];

  // Portal to <body> so page-level resets (e.g. .landing *) don't reach the modal.
  return createPortal(
    <div className="demo-backdrop" onClick={onClose}>
      <div className="demo-modal" role="dialog" aria-modal="true" aria-labelledby="demo-title" onClick={(e) => e.stopPropagation()}>
        <div className="demo-head">
          <h2 id="demo-title">See AgriConnect in action</h2>
          <button type="button" className="demo-close" onClick={onClose} aria-label="Close demo" autoFocus>×</button>
        </div>

        <div className="demo-tabs" role="tablist">
          <button type="button" role="tab" aria-selected={tab === 'video'} className={tab === 'video' ? 'active' : ''} onClick={() => setTab('video')}>
            ▶ Video walkthrough
          </button>
          <button type="button" role="tab" aria-selected={tab === 'tour'} className={tab === 'tour' ? 'active' : ''} onClick={() => setTab('tour')}>
            Guided tour
          </button>
        </div>

        {tab === 'video' ? (
          <div className="demo-stage">
            <img src="/demo/walkthrough.webp" alt="Walkthrough of AgriConnect: dashboard, farms and weather, crops, inventory, finance, marketplace and escrow orders" />
          </div>
        ) : (
          <div className="demo-tour">
            <div className="demo-stage">
              <img src={current.src} alt={current.title} />
            </div>
            <div className="demo-caption">
              <small>Step {step + 1} of {TOUR_STEPS.length}</small>
              <h3>{current.title}</h3>
              <p>{current.text}</p>
            </div>
            <div className="demo-tour-nav">
              <button type="button" className="demo-btn" disabled={step === 0} onClick={() => setStep(step - 1)}>← Back</button>
              <div className="demo-dots">
                {TOUR_STEPS.map((s, i) => (
                  <button key={s.src} type="button" aria-label={`Step ${i + 1}`} className={i === step ? 'active' : ''} onClick={() => setStep(i)} />
                ))}
              </div>
              <button type="button" className="demo-btn" disabled={step === TOUR_STEPS.length - 1} onClick={() => setStep(step + 1)}>Next →</button>
            </div>
          </div>
        )}

        <div className="demo-foot">
          <p><strong>Explore it yourself.</strong> Open a ready-made demo account. No sign-up needed.</p>
          <TryDemoButtons />
        </div>
      </div>
    </div>,
    document.body
  );
}
