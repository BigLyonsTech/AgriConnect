import httpClient from './httpClient';

export const listExpenses = (farmId) =>
  httpClient.get('/api/finance/expenses', { params: farmId ? { farmId } : {} }).then((r) => r.data);
export const createExpense = (payload) => httpClient.post('/api/finance/expenses', payload).then((r) => r.data);
export const deleteExpense = (id) => httpClient.delete(`/api/finance/expenses/${id}`);

export const listRevenues = (farmId) =>
  httpClient.get('/api/finance/revenues', { params: farmId ? { farmId } : {} }).then((r) => r.data);
export const createRevenue = (payload) => httpClient.post('/api/finance/revenues', payload).then((r) => r.data);
export const deleteRevenue = (id) => httpClient.delete(`/api/finance/revenues/${id}`);
