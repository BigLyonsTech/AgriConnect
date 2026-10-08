import httpClient from './httpClient';

export const getDashboardSummary = () => httpClient.get('/api/analytics/dashboard').then((r) => r.data);
