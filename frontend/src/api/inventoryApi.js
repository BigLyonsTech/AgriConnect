import httpClient from './httpClient';

export const listInventory = (farmId) =>
  httpClient.get('/api/inventory', { params: farmId ? { farmId } : {} }).then((r) => r.data);
export const createInventoryItem = (payload) => httpClient.post('/api/inventory', payload).then((r) => r.data);
export const deleteInventoryItem = (id) => httpClient.delete(`/api/inventory/${id}`);

export const listYieldRecords = (cropId) =>
  httpClient.get('/api/yield-records', { params: cropId ? { cropId } : {} }).then((r) => r.data);
export const createYieldRecord = (payload) => httpClient.post('/api/yield-records', payload).then((r) => r.data);
