import { NavLink } from 'react-router-dom';
import { useAuth } from '../features/auth/AuthContext';
import '../styles/applayout.css';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/farms', label: 'Farms' },
  { to: '/crops-livestock', label: 'Crops & Livestock' },
  { to: '/inventory', label: 'Inventory' },
  { to: '/finance', label: 'Finance' },
  { to: '/marketplace', label: 'Marketplace' },
  { to: '/orders', label: 'Orders' },
];

export default function AppLayout({ children }) {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-logo">
          🌱 Agri<span>Connect</span>
        </div>
        <nav className="app-nav">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => 'app-nav-link' + (isActive ? ' active' : '')}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="app-user">
          <div>
            <strong>{user?.fullName}</strong>
            <small>{user?.role} · {user?.organizationName}</small>
          </div>
          <button onClick={logout} className="app-logout">Log Out</button>
        </div>
      </header>
      <main className="app-main">{children}</main>
    </div>
  );
}
