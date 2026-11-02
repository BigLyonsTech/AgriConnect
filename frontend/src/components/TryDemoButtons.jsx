import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../features/auth/AuthContext';
import { extractErrorMessage } from '../utils/errors';

// "Try the live demo" as the seller (farmer) or the buyer side of the marketplace.
export default function TryDemoButtons({ className = '' }) {
  const { loginAsDemo } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(null);
  const [error, setError] = useState('');

  async function start(as) {
    setLoading(as);
    setError('');
    try {
      await loginAsDemo(as);
      navigate(as === 'buyer' ? '/marketplace' : '/dashboard');
    } catch (err) {
      setError(extractErrorMessage(err, 'The live demo is unavailable right now.'));
      setLoading(null);
    }
  }

  return (
    <div className={'try-demo ' + className}>
      <div className="try-demo-buttons">
        <button type="button" className="demo-btn demo-btn-primary" disabled={loading !== null} onClick={() => start('farmer')}>
          {loading === 'farmer' ? 'Opening…' : 'Try as a farmer'}
        </button>
        <button type="button" className="demo-btn" disabled={loading !== null} onClick={() => start('buyer')}>
          {loading === 'buyer' ? 'Opening…' : 'Try as a buyer'}
        </button>
      </div>
      {error && <p className="try-demo-error" role="alert">{error}</p>}
    </div>
  );
}
