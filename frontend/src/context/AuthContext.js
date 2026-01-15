import React, { createContext, useState, useContext, useEffect } from 'react';
import { authAPI } from '../services/api';
import websocketService from '../services/websocket';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  // Initialize user from localStorage to avoid null state during navigation
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('token');
    return token ? { token } : null;
  });
  const [loading, setLoading] = useState(true);

  // Lightweight JWT decoder to extract subject (username)
  const decodeToken = (token) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''));
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('[AuthContext] Failed to decode token:', error);
      return null;
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token');
    console.log('[AuthContext] Initial token check:', token ? 'Token exists' : 'No token');
    if (token) {
      // Token exists, validate it
      authAPI.validate(token)
        .then(() => {
          console.log('[AuthContext] Token validated successfully');
          const decoded = decodeToken(token);
          const username = decoded?.sub || decoded?.username || null;
          const role = decoded?.role || 'user';
          
          setUser({ token, username, role });
        })
        .catch((error) => {
          console.error('[AuthContext] Token validation failed:', error.response?.status, error.message);
          localStorage.removeItem('token');
          setUser(null);
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (username, password) => {
    try {
      const response = await authAPI.login({ username, password });
      const token = response.data;
      localStorage.setItem('token', token);
      
      const decoded = decodeToken(token);
      const role = decoded?.role || 'user';
      
      setUser({ username, token, role });
      
      return { success: true };
    } catch (error) {
      return { success: false, error: error.response?.data?.message || 'Login failed. Please check your credentials.' };
    }
  };

  const logout = () => {
    console.log('[AuthContext] Logout called');
    websocketService.disconnect();
    localStorage.removeItem('token');
    setUser(null);
  };

  return <AuthContext.Provider value={{ user, login, logout, loading }}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
