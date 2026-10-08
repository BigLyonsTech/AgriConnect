import httpClient from './httpClient';

export const initiatePayment = (payload) => httpClient.post('/api/payments/initiate', payload).then((r) => r.data);
export const releaseFunds = (orderId) => httpClient.post(`/api/payments/orders/${orderId}/release`);
