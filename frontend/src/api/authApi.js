import httpClient from './httpClient';

/**
 * @param {{ organizationName: string, email: string, password: string, fullName: string, role?: string }} payload
 * @returns {Promise<{ token: string, tokenType: string, userId: number, email: string,
 *   fullName: string, role: string, organizationId: number, organizationName: string }>}
 */
export async function registerOrganization(payload) {
  const response = await httpClient.post('/api/auth/register', payload);
  return response.data;
}

/**
 * @param {{ email: string, password: string }} payload
 */
export async function login(payload) {
  const response = await httpClient.post('/api/auth/login', payload);
  return response.data;
}

/**
 * One-click live demo. The backend seeds the demo orgs on first use.
 * @param {'farmer' | 'buyer'} as
 */
export async function demoLogin(as = 'farmer') {
  const response = await httpClient.post('/api/auth/demo', null, { params: as === 'buyer' ? { as } : {} });
  return response.data;
}
