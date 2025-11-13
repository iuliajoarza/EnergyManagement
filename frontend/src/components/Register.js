import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authAPI } from '../services/api';

const ROLE_OPTIONS = [
  { value: 'ROLE_USER', label: 'User' },
  { value: 'ROLE_ADMIN', label: 'Admin' },
];

const Register = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '', confirmPassword: '', role: ROLE_OPTIONS[0].value });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');

  const handleChange = (key) => (event) => {
    setForm((prev) => ({ ...prev, [key]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    if (event && typeof event.preventDefault === 'function') event.preventDefault();
    console.debug('[Register] Submit clicked');
    setError('');
    setInfo('');

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        username: form.username.trim(),
        password: form.password,
        role: form.role,
      };
      const response = await authAPI.register(payload);
      const data = response?.data;
      if (data?.success === false) {
        setError(data?.message || 'Registration failed.');
        return;
      }
      const successMessage = data?.message || 'Account created. Redirecting to login…';
      setInfo(successMessage);
      setForm({ username: '', password: '', confirmPassword: '', role: ROLE_OPTIONS[0].value });
      setTimeout(() => navigate('/login'), 1200);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Registration failed.');
    } finally {
      setSubmitting(false);
    }
  };

  const styles = {
    container: { maxWidth: '420px', margin: '3rem auto', padding: '2rem', background: '#fff', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)' },
    header: { marginBottom: '1.5rem', textAlign: 'center' },
    field: { width: '100%', marginBottom: '1rem', padding: '0.65rem', border: '1px solid #dcdcdc', borderRadius: '4px' },
    button: { width: '100%', padding: '0.75rem', border: 'none', borderRadius: '4px', background: '#2ecc71', color: '#fff', fontWeight: 600, cursor: 'pointer' },
    link: { marginTop: '1rem', textAlign: 'center', display: 'block', color: '#3498db' },
    alert: (color, bg) => ({ marginBottom: '1rem', padding: '0.75rem', borderRadius: '4px', color, background: bg }),
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.header}>Create an account</h2>

      {error && <div style={styles.alert('#721c24', '#f8d7da')}>{error}</div>}
      {info && <div style={styles.alert('#155724', '#d4edda')}>{info}</div>}

      <form onSubmit={handleSubmit} noValidate>
        <input
          style={styles.field}
          type="text"
          placeholder="Username"
          value={form.username}
          onChange={handleChange('username')}
          required
          minLength={3}
        />
        <input
          style={styles.field}
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange('password')}
          required
          minLength={6}
        />
        <input
          style={styles.field}
          type="password"
          placeholder="Confirm password"
          value={form.confirmPassword}
          onChange={handleChange('confirmPassword')}
          required
          minLength={6}
        />
        <select style={styles.field} value={form.role} onChange={handleChange('role')}>
          {ROLE_OPTIONS.map(({ value, label }) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
        <button style={styles.button} type="submit" disabled={submitting}>
          {submitting ? 'Creating…' : 'Register'}
        </button>
      </form>

      <button style={{ ...styles.button, marginTop: '1rem', background: '#95a5a6' }} type="button" onClick={() => navigate('/login')}>
        Back to login
      </button>
    </div>
  );
};

export default Register;
