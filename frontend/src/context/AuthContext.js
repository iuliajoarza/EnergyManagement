import React, { createContext, useState, useContext, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  // Initialize user from localStorage to avoid null state during navigation
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('token');
    return token ? { token } : null;
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    console.log('[AuthContext] Initial token check:', token ? 'Token exists' : 'No token');
    if (token) {
      // Token exists, validate it
      authAPI.validate(token)
        .then(() => {
          console.log('[AuthContext] Token validated successfully');
          setUser({ token });
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
      localStorage.setItem('token', response.data);
      setUser({ username, token: response.data });
      return { success: true };
    } catch (error) {
      return { success: false, error: error.response?.data?.message || 'Login failed. Please check your credentials.' };
    }
  };

  const logout = () => {
    console.log('[AuthContext] Logout called');
    console.trace('[AuthContext] Logout stack trace');
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
