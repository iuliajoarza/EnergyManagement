import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();

  console.log('[ProtectedRoute] Checking auth:', { user: !!user, loading, hasToken: !!localStorage.getItem('token') });

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

  // Check both user state AND localStorage token as a safeguard
  const hasToken = localStorage.getItem('token');
  const isAuthenticated = user || hasToken;

  if (!isAuthenticated) {
    console.log('[ProtectedRoute] Not authenticated, redirecting to login');
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;
