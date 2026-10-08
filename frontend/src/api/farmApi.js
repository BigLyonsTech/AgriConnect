import httpClient from './httpClient';

export const listFarms = () => httpClient.get('/api/farms').then((r) => r.data);
export const createFarm = (payload) => httpClient.post('/api/farms', payload).then((r) => r.data);
export const updateFarm = (id, payload) => httpClient.put(`/api/farms/${id}`, payload).then((r) => r.data);
export const deleteFarm = (id) => httpClient.delete(`/api/farms/${id}`);

export const listCrops = (farmId) =>
  httpClient.get('/api/crops', { params: farmId ? { farmId } : {} }).then((r) => r.data);
export const createCrop = (payload) => httpClient.post('/api/crops', payload).then((r) => r.data);
export const deleteCrop = (id) => httpClient.delete(`/api/crops/${id}`);

export const listLivestock = (farmId) =>
  httpClient.get('/api/livestock', { params: farmId ? { farmId } : {} }).then((r) => r.data);
export const createLivestock = (payload) => httpClient.post('/api/livestock', payload).then((r) => r.data);
export const deleteLivestock = (id) => httpClient.delete(`/api/livestock/${id}`);

export const getFarmWeather = (farmId) => httpClient.get(`/api/farms/${farmId}/weather`).then((r) => r.data);
