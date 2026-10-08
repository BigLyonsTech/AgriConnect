import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { extractErrorMessage } from '../../utils/errors';
import '../../styles/auth.css';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    organizationName: '',
    fullName: '',
    email: '',
    password: '',
    role: 'OWNER',
  });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (event) => {
    setForm((prev) => ({ ...prev, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await register(form);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, 'Could not create your account.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <Link to="/" className="auth-logo">
          🌱 Agri<span>Connect</span>
        </Link>
        <h1>Create your account</h1>
        <p className="auth-subtitle">
          Register a farm or cooperative to sell produce, or a buyer account to purchase from the marketplace.
        </p>

        {error && <div className="auth-error">{error}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <fieldset className="auth-role-group">
            <legend>I want to join as…</legend>
            <label className={`auth-role-option ${form.role === 'OWNER' ? 'selected' : ''}`}>
              <input
                type="radio"
                name="role"
                value="OWNER"
                checked={form.role === 'OWNER'}
                onChange={handleChange}
              />
              <span>
                <strong>Farmer / Cooperative</strong>
                <small>Manage farms, crops, inventory, and sell produce.</small>
              </span>
            </label>
            <label className={`auth-role-option ${form.role === 'BUYER' ? 'selected' : ''}`}>
              <input
                type="radio"
                name="role"
                value="BUYER"
                checked={form.role === 'BUYER'}
                onChange={handleChange}
              />
              <span>
                <strong>Buyer / Market trader</strong>
                <small>Browse the marketplace and purchase produce.</small>
              </span>
            </label>
          </fieldset>

          <label htmlFor="organizationName">Organization name</label>
          <input
            id="organizationName"
            name="organizationName"
            type="text"
            required
            value={form.organizationName}
            onChange={handleChange}
            placeholder="Sunrise Cooperative"
          />

          <label htmlFor="fullName">Your full name</label>
          <input
            id="fullName"
            name="fullName"
            type="text"
            required
            value={form.fullName}
            onChange={handleChange}
            placeholder="Kwame Mensah"
          />

          <label htmlFor="email">Email</label>
          <input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            required
            value={form.email}
            onChange={handleChange}
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            name="password"
            type="password"
            autoComplete="new-password"
            required
            minLength={8}
            value={form.password}
            onChange={handleChange}
          />
          <span className="auth-hint">At least 8 characters.</span>

          <button type="submit" className="auth-submit" disabled={submitting}>
            {submitting ? 'Creating account…' : 'Create Account'}
          </button>
        </form>

        <p className="auth-switch">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
}
