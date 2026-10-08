import httpClient from './httpClient';

export const browseListings = () => httpClient.get('/api/marketplace/listings').then((r) => r.data);
export const myListings = () => httpClient.get('/api/marketplace/listings/mine').then((r) => r.data);
export const createListing = (payload) => httpClient.post('/api/marketplace/listings', payload).then((r) => r.data);
export const closeListing = (id) => httpClient.delete(`/api/marketplace/listings/${id}`);

export const placeOrder = (payload) => httpClient.post('/api/marketplace/orders', payload).then((r) => r.data);
export const myOrders = () => httpClient.get('/api/marketplace/orders/mine').then((r) => r.data);
export const ordersAgainstMyListings = () =>
  httpClient.get('/api/marketplace/orders/against-my-listings').then((r) => r.data);
export const markOrderFulfilled = (id) =>
  httpClient.post(`/api/marketplace/orders/${id}/fulfill`).then((r) => r.data);
