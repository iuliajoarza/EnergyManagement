import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Decode JWT token to extract role
const decodeToken = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('[AdminRoute] Failed to decode token:', error);
    return null;
  }
};

const AdminRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        fontSize: '1.2rem',
      }}>
        Loading...
      </div>
    );
  }

  const token = user?.token || localStorage.getItem('token');
  
  if (!token) {
    console.log('[AdminRoute] No token found, redirecting to login');
    return <Navigate to="/login" replace />;
  }

  const decoded = decodeToken(token);
  const role = decoded?.role;

  console.log('[AdminRoute] Checking admin access:', { role, hasAdminRole: role === 'ROLE_ADMIN' });

  if (role !== 'ROLE_ADMIN') {
    console.log('[AdminRoute] Access denied - not an admin, redirecting to devices');
    return <Navigate to="/devices" replace />;
  }

  return children;
};

export default AdminRoute;
