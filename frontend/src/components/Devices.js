import React, { useState, useEffect } from 'react';
import { devicesAPI, peopleAPI } from '../services/api';

const Devices = () => {
  const [devices, setDevices] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingDevice, setEditingDevice] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    maxConsumption: '',
    userId: ''
  });

  useEffect(() => {
    fetchDevices();
    fetchUsers();
  }, []);

  const fetchDevices = async () => {
    try {
      const response = await devicesAPI.getAll();
      setDevices(response.data);
    } catch (err) {
      setError('Failed to fetch devices');
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    try {
      const response = await peopleAPI.getAll();
      setUsers(Array.isArray(response.data) ? response.data : [response.data]);
    } catch (err) {
      console.error('Failed to fetch users', err);
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      maxConsumption: '',
      userId: ''
    });
    setEditingDevice(null);
    setShowForm(false);
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        name: formData.name,
        maxConsumption: parseFloat(formData.maxConsumption),
        userId: formData.userId || null // Allow null userId for unassigned devices
      };

      if (editingDevice) {
        await devicesAPI.update(editingDevice.id, payload);
      } else {
        await devicesAPI.create(payload);
      }

      fetchDevices();
      resetForm();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save device');
    }
  };

  const handleEdit = (device) => {
    setEditingDevice(device);
    setFormData({
      name: device.name || '',
      maxConsumption: device.maxConsumption?.toString() || '',
      userId: device.userId || ''
    });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this device?')) return;
    try {
      await devicesAPI.delete(id);
      fetchDevices();
    } catch (err) {
      setError('Failed to delete device');
    }
  };

  const getUserName = (userId) => {
    const user = users.find((u) => u.id === userId);
    return user ? user.name : 'Unassigned';
  };

  const styles = {
    container: { maxWidth: '900px', margin: '0 auto', padding: '2rem' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' },
    button: { backgroundColor: '#3498db', color: 'white', border: 'none', padding: '0.5rem 1rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.9rem' },
    deleteButton: { backgroundColor: '#e74c3c', marginLeft: '0.5rem' },
    table: { width: '100%', borderCollapse: 'collapse', marginTop: '1rem' },
    th: { textAlign: 'left', padding: '1rem', borderBottom: '1px solid #ddd' },
    td: { padding: '1rem', borderBottom: '1px solid #ddd' },
    form: { backgroundColor: 'white', padding: '2rem', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', marginBottom: '2rem' },
    formRow: { display: 'flex', gap: '1rem', marginBottom: '1rem' },
    input: { flex: 1, padding: '0.5rem', border: '1px solid #ddd', borderRadius: '4px' },
    error: { backgroundColor: '#f8d7da', color: '#721c24', padding: '1rem', borderRadius: '4px', marginBottom: '1rem' }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h2>Device Management</h2>
        <button style={styles.button} onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Add Device'}
        </button>
      </div>

      {error && <div style={styles.error}>{error}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} style={styles.form}>
          <h3>{editingDevice ? 'Edit Device' : 'Add New Device'}</h3>
          <div style={styles.formRow}>
            <input
              type="text"
              placeholder="Device Name"
              value={formData.name}
              onChange={(e) => setFormData((prev) => ({ ...prev, name: e.target.value }))}
              style={styles.input}
              required
            />
            <input
              type="number"
              placeholder="Max Consumption (kW)"
              value={formData.maxConsumption}
              onChange={(e) => setFormData((prev) => ({ ...prev, maxConsumption: e.target.value }))}
              style={styles.input}
              required
              step="0.1"
              min="0"
            />
            <select
              value={formData.userId}
              onChange={(e) => setFormData((prev) => ({ ...prev, userId: e.target.value }))}
              style={styles.input}
            >
              <option value="">-- No User (Unassigned) --</option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.name} ({user.id})
                </option>
              ))}
            </select>
          </div>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button type="submit" style={styles.button}>
              {editingDevice ? 'Update' : 'Create'}
            </button>
            <button type="button" style={{ ...styles.button, backgroundColor: '#95a5a6' }} onClick={resetForm}>
              Cancel
            </button>
          </div>
        </form>
      )}

      <table style={styles.table}>
        <thead>
          <tr>
            <th style={styles.th}>Name</th>
            <th style={styles.th}>Max Consumption (kW)</th>
            <th style={styles.th}>Assigned User</th>
            <th style={styles.th}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {devices.map((device) => (
            <tr key={device.id}>
              <td style={styles.td}>{device.name}</td>
              <td style={styles.td}>{device.maxConsumption}</td>
              <td style={styles.td}>{getUserName(device.userId)}</td>
              <td style={styles.td}>
                <button style={styles.button} onClick={() => handleEdit(device)}>
                  Edit
                </button>
                <button style={{ ...styles.button, ...styles.deleteButton }} onClick={() => handleDelete(device.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Devices;
