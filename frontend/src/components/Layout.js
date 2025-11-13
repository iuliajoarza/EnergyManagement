import React from 'react';
import { Outlet, Link } from 'react-router-dom';
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
    console.error('[Layout] Failed to decode token:', error);
    return null;
  }
};

const Layout = () => {
  const { logout, user } = useAuth(); // added user to display username if needed

  // Decode token to get role
  const token = user?.token || localStorage.getItem('token');
  const decoded = token ? decodeToken(token) : null;
  const isAdmin = decoded?.role === 'ROLE_ADMIN';

  const styles = {
    container: {
      minHeight: '100vh',
      backgroundColor: '#f5f5f5',
    },
    header: {
      backgroundColor: '#2c3e50',
      color: 'white',
      padding: '1rem 2rem',
    },
    nav: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      maxWidth: '1200px',
      margin: '0 auto',
    },
    navLinks: {
      display: 'flex',
      gap: '2rem',
    },
    navLink: {
      color: 'white',
      textDecoration: 'none',
      padding: '0.5rem 1rem',
      transition: 'background-color 0.2s',
    },
    logoutButton: {
      backgroundColor: '#e74c3c',
      color: 'white',
      border: 'none',
      padding: '0.5rem 1.5rem',
      borderRadius: '4px',
      cursor: 'pointer',
      fontSize: '0.9rem',
      fontWeight: '500',
    },
    main: {
      maxWidth: '1200px',
      margin: '2rem auto',
      padding: '0 2rem',
    },
    userInfo: {
      display: 'flex',
      alignItems: 'center',
      gap: '1rem',
    },
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <nav style={styles.nav}>
          <div style={styles.navLinks}>
            {isAdmin && <Link to="/people" style={styles.navLink}>People</Link>}
            <Link to="/devices" style={styles.navLink}>Devices</Link>
          </div>
          <div style={styles.userInfo}>
            <span>Welcome, {user?.username || 'User'}</span>
            <button style={styles.logoutButton} onClick={logout}>Logout</button>
          </div>
        </nav>
      </header>
      <main style={styles.main}>
        <Outlet />
      </main>
    </div>
  );
};

export default Layout;
