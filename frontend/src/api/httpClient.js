import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const httpClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach the stored JWT (if any) to every outgoing request.
httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('agriconnect_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the token is rejected or expired, clear it and let the app redirect to login.
httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('agriconnect_token');
      localStorage.removeItem('agriconnect_user');
    }
    return Promise.reject(error);
  }
);

export default httpClient;
