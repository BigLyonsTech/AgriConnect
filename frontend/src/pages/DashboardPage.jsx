import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import AppLayout from '../components/AppLayout';
import { useAuth } from '../features/auth/AuthContext';
import { getDashboardSummary } from '../api/analyticsApi';
import { extractErrorMessage } from '../utils/errors';
import '../styles/dashboard.css';

export default function DashboardPage() {
  const { user } = useAuth();
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getDashboardSummary().then(setSummary).catch((err) => setError(extractErrorMessage(err)));
  }, []);

  return (
    <AppLayout>
      <div className="page-header">
        <h1>Welcome, {user?.fullName?.split(' ')[0]} 👋</h1>
      </div>
      <p style={{ color: '#61736c', marginBottom: 24 }}>
        You're logged in to <strong>{user?.organizationName}</strong> as <strong>{user?.role}</strong>.
      </p>

      {error && <div className="page-error">{error}</div>}

      {summary && (
        <div className="summary-grid">
          <div className="summary-card"><small>Farms</small><strong>{summary.totalFarms}</strong></div>
          <div className="summary-card"><small>Crops</small><strong>{summary.totalCrops}</strong></div>
          <div className="summary-card"><small>Inventory</small><strong>{summary.totalInventoryKg.toLocaleString()} kg</strong></div>
          <div className="summary-card"><small>Revenue</small><strong>₦{Number(summary.totalRevenue).toLocaleString()}</strong></div>
          <div className="summary-card"><small>Expenses</small><strong>₦{Number(summary.totalExpenses).toLocaleString()}</strong></div>
          <div className="summary-card profit"><small>Net Profit</small><strong>₦{Number(summary.netProfit).toLocaleString()}</strong></div>
          <div className="summary-card"><small>Active Listings</small><strong>{summary.activeListings}</strong></div>
          <div className="summary-card"><small>Pending Orders</small><strong>{summary.pendingOrdersAsSeller}</strong></div>
        </div>
      )}

      <div className="dashboard-placeholder-grid">
        <Link to="/farms" className="placeholder-card" style={{ textDecoration: 'none' }}>
          <h3>🌾 Farms</h3>
          <p>Manage your farms</p>
        </Link>
        <Link to="/crops-livestock" className="placeholder-card" style={{ textDecoration: 'none' }}>
          <h3>🌽 Crops & Livestock</h3>
          <p>Track what's growing</p>
        </Link>
        <Link to="/inventory" className="placeholder-card" style={{ textDecoration: 'none' }}>
          <h3>📦 Inventory</h3>
          <p>Manage stock levels</p>
        </Link>
        <Link to="/finance" className="placeholder-card" style={{ textDecoration: 'none' }}>
          <h3>💰 Finance</h3>
          <p>Track income & expenses</p>
        </Link>
        <Link to="/marketplace" className="placeholder-card" style={{ textDecoration: 'none' }}>
          <h3>🛒 Marketplace</h3>
          <p>Buy & sell produce</p>
        </Link>
        <Link to="/orders" className="placeholder-card" style={{ textDecoration: 'none' }}>
          <h3>📬 Orders</h3>
          <p>Payments & fulfillment</p>
        </Link>
      </div>
    </AppLayout>
  );
}
