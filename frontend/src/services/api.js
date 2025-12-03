import axios from 'axios';

// Helper to decode JWT
export const decodeJWT = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
};

const baseURL = (process.env.REACT_APP_API_URL || 'http://localhost').replace(/\/+$/, '');

const apiClient = axios.create({
  baseURL,
  withCredentials: true,
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    console.log(`[API] Request interceptor - Token exists: ${!!token}`);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`[API] Added Authorization header: Bearer ${token.substring(0, 20)}...`);
    } else {
      console.warn('[API] No token found in localStorage!');
    }
    console.log(`[API] Request: ${config.method?.toUpperCase()} ${baseURL}${config.url}`, {
      Authorization: config.headers.Authorization ? 'Present' : 'Missing',
      ContentType: config.headers['Content-Type']
    });
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => {
    console.log(`[API] Response ${response.status}:`, response.data);
    return response;
  },
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url;
    console.error(`[API] Error ${status} on ${url}:`, error.response?.data);
    console.error('[API] Full error details:', error);
    
    // TEMPORARILY DISABLED AUTO-LOGOUT TO SEE THE ERROR
    // Only logout on 401 for authentication endpoints or if token is truly invalid
    // Don't logout on 403 (forbidden) or other authorization issues
    // if (status === 401 && localStorage.getItem('token')) {
    //   console.warn('[API] 401 Unauthorized - removing token and redirecting to login');
    //   localStorage.removeItem('token');
    //   // Use a longer timeout to prevent multiple rapid redirects
    //   setTimeout(() => {
    //     if (!window.location.pathname.includes('/login')) {
    //       window.location.href = '/login';
    //     }
    //   }, 500);
    // }
    return Promise.reject(error);
  }
);

// Public API for login/register
const publicApiClient = axios.create({
  baseURL,
  withCredentials: true,
});

export const authAPI = {
  login: (payload) => publicApiClient.post('/api/auth/login', payload),
  register: (payload) => publicApiClient.post('/api/auth/register', payload),
  validate: (token) => publicApiClient.get('/api/auth/validate', { params: { token } }),
};

export const peopleAPI = {
  getAll: () => apiClient.get('/api/user'),
  getById: (id) => apiClient.get(`/api/user/${id}`),
  getByUsername: (username) => apiClient.get('/api/user', { params: { username } }),
  create: (user) => apiClient.post('/api/user', user),
  update: (id, user) => apiClient.put(`/api/user/${id}`, user),
  delete: (id) => apiClient.delete(`/api/user/${id}`),
};

export const devicesAPI = {
  getAll: () => apiClient.get('/api/device'),
  getById: (id) => apiClient.get(`/api/device/${id}`),
  getByUserId: (userId) => apiClient.get('/api/device', { params: { userId } }),
  create: (device) => apiClient.post('/api/device', device),
  update: (id, device) => apiClient.put(`/api/device/${id}`, device),
  delete: (id) => apiClient.delete(`/api/device/${id}`),
  deleteByUser: (userId) => apiClient.delete(`/api/device/user/${userId}`),
  getAllUsers: () => apiClient.get('/api/device/users'),
  getUserIdByUsername: (username) => apiClient.get('/api/device/users/by-username', { params: { username } })
};

// API pentru user cache din microserviciul device
export const userCacheAPI = {
  getAll: () => apiClient.get('/api/device/users'),
  getByUsername: (username) => apiClient.get('/api/device/users/by-username', { params: { username } })
};

export default apiClient;
