import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './features/auth/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LandingPage from './pages/LandingPage';
import LoginPage from './features/auth/LoginPage';
import RegisterPage from './features/auth/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import FarmsPage from './pages/FarmsPage';
import CropsLivestockPage from './pages/CropsLivestockPage';
import InventoryPage from './pages/InventoryPage';
import FinancePage from './pages/FinancePage';
import MarketplacePage from './pages/MarketplacePage';
import OrdersPage from './pages/OrdersPage';

function Protected({ children }) {
  return <ProtectedRoute>{children}</ProtectedRoute>;
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route path="/dashboard" element={<Protected><DashboardPage /></Protected>} />
          <Route path="/farms" element={<Protected><FarmsPage /></Protected>} />
          <Route path="/crops-livestock" element={<Protected><CropsLivestockPage /></Protected>} />
          <Route path="/inventory" element={<Protected><InventoryPage /></Protected>} />
          <Route path="/finance" element={<Protected><FinancePage /></Protected>} />
          <Route path="/marketplace" element={<Protected><MarketplacePage /></Protected>} />
          <Route path="/orders" element={<Protected><OrdersPage /></Protected>} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
