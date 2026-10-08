import { createContext, useContext, useMemo, useState } from 'react';
import { login as loginRequest, registerOrganization } from '../../api/authApi';

const AuthContext = createContext(null);

function readStoredUser() {
  const raw = localStorage.getItem('agriconnect_user');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function persistSession(authResponse) {
  localStorage.setItem('agriconnect_token', authResponse.token);
  localStorage.setItem(
    'agriconnect_user',
    JSON.stringify({
      userId: authResponse.userId,
      email: authResponse.email,
      fullName: authResponse.fullName,
      role: authResponse.role,
      organizationId: authResponse.organizationId,
      organizationName: authResponse.organizationName,
    })
  );
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);
  const [token, setToken] = useState(() => localStorage.getItem('agriconnect_token'));

  const login = async (email, password) => {
    const data = await loginRequest({ email, password });
    persistSession(data);
    setToken(data.token);
    setUser({
      userId: data.userId,
      email: data.email,
      fullName: data.fullName,
      role: data.role,
      organizationId: data.organizationId,
      organizationName: data.organizationName,
    });
    return data;
  };

  const register = async ({ organizationName, email, password, fullName, role }) => {
    const data = await registerOrganization({ organizationName, email, password, fullName, role });
    persistSession(data);
    setToken(data.token);
    setUser({
      userId: data.userId,
      email: data.email,
      fullName: data.fullName,
      role: data.role,
      organizationId: data.organizationId,
      organizationName: data.organizationName,
    });
    return data;
  };

  const logout = () => {
    localStorage.removeItem('agriconnect_token');
    localStorage.removeItem('agriconnect_user');
    setToken(null);
    setUser(null);
  };

  const value = useMemo(
    () => ({ user, token, isAuthenticated: Boolean(token), login, register, logout }),
    [user, token]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
