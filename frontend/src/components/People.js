import React, { useState, useEffect } from 'react';
import { peopleAPI } from '../services/api';

const People = () => {
  const [people, setPeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingPerson, setEditingPerson] = useState(null);
  const [formData, setFormData] = useState({ name: '', username: '', address: '', age: '' });

  useEffect(() => {
    fetchPeople();
  }, []);

  const fetchPeople = async () => {
    try {
      const response = await peopleAPI.getAll();
      const data = response.data;
      // Backend returns single object for users, array for admins
      setPeople(Array.isArray(data) ? data : (data ? [data] : []));
    } catch (err) {
      setError('Failed to fetch people');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const personData = { ...formData, age: parseInt(formData.age) };
      
      if (editingPerson) {
        await peopleAPI.update(editingPerson.id, { ...personData, id: editingPerson.id });
      } else {
        await peopleAPI.create(personData);
      }
      
      fetchPeople();
      resetForm();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save person');
    }
  };

  const handleEdit = async (person) => {
    try {
      setEditingPerson(person);
      // Fetch full details to avoid missing fields from list DTO
      const res = await peopleAPI.getById(person.id);
      const full = res.data || {};
      setFormData({
        name: full.name ?? person.name ?? '',
        username: full.username ?? person.username ?? '',
        address: full.address ?? person.address ?? '',
        age: (full.age ?? person.age ?? '').toString(),
      });
      setShowForm(true);
    } catch (e) {
      // Fallback to whatever we have without crashing
      setFormData({
        name: person?.name ?? '',
        username: person?.username ?? '',
        address: person?.address ?? '',
        age: (person?.age ?? '').toString(),
      });
      setShowForm(true);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this person?')) {
      try {
        await peopleAPI.delete(id);
        fetchPeople();
      } catch (err) {
        setError('Failed to delete person');
      }
    }
  };

  const resetForm = () => {
    setFormData({ name: '', username: '', address: '', age: '' });
    setEditingPerson(null);
    setShowForm(false);
    setError('');
  };

  const styles = {
    container: {
      maxWidth: '800px',
      margin: '0 auto',
    },
    header: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      marginBottom: '2rem',
    },
    button: {
      backgroundColor: '#3498db',
      color: 'white',
      border: 'none',
      padding: '0.5rem 1rem',
      borderRadius: '4px',
      cursor: 'pointer',
      fontSize: '0.9rem',
    },
    deleteButton: {
      backgroundColor: '#e74c3c',
      marginLeft: '0.5rem',
    },
    table: {
      width: '100%',
      borderCollapse: 'collapse',
      backgroundColor: 'white',
      borderRadius: '8px',
      overflow: 'hidden',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    },
    th: {
      backgroundColor: '#f8f9fa',
      padding: '1rem',
      textAlign: 'left',
      fontWeight: '600',
    },
    td: {
      padding: '1rem',
      borderTop: '1px solid #eee',
    },
    form: {
      backgroundColor: 'white',
      padding: '2rem',
      borderRadius: '8px',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
      marginBottom: '2rem',
    },
    formRow: {
      display: 'flex',
      gap: '1rem',
      marginBottom: '1rem',
    },
    input: {
      flex: 1,
      padding: '0.5rem',
      border: '1px solid #ddd',
      borderRadius: '4px',
    },
    error: {
      backgroundColor: '#f8d7da',
      color: '#721c24',
      padding: '1rem',
      borderRadius: '4px',
      marginBottom: '1rem',
    },
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h2>People Management</h2>
        <button
          style={styles.button}
          onClick={() => setShowForm(!showForm)}
        >
          {showForm ? 'Cancel' : 'Add Person'}
        </button>
      </div>

      {error && <div style={styles.error}>{error}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} style={styles.form}>
          <h3>{editingPerson ? 'Edit Person' : 'Add New Person'}</h3>
          <div style={styles.formRow}>
            <input
              type="text"
              placeholder="Name"
              value={formData.name}
              onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
              required
              style={styles.input}
            />
            <input
              type="text"
              placeholder="Username"
              value={formData.username}
              onChange={(e) => setFormData(prev => ({ ...prev, username: e.target.value }))}
              required
              style={styles.input}
            />
            <input
              type="text"
              placeholder="Address"
              value={formData.address}
              onChange={(e) => setFormData(prev => ({ ...prev, address: e.target.value }))}
              required
              style={styles.input}
            />
            <input
              type="number"
              placeholder="Age"
              value={formData.age}
              onChange={(e) => setFormData(prev => ({ ...prev, age: e.target.value }))}
              required
              min="18"
              style={styles.input}
            />
          </div>
          <div>
            <button type="submit" style={styles.button}>
              {editingPerson ? 'Update' : 'Create'}
            </button>
            <button
              type="button"
              onClick={resetForm}
              style={{ ...styles.button, backgroundColor: '#95a5a6', marginLeft: '0.5rem' }}
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      <table style={styles.table}>
        <thead>
          <tr>
            <th style={styles.th}>Name</th>
            <th style={styles.th}>Username</th>
            <th style={styles.th}>Address</th>
            <th style={styles.th}>Age</th>
            <th style={styles.th}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {people.map((person) => (
            <tr key={person.id}>
              <td style={styles.td}>{person.name}</td>
              <td style={styles.td}>{person.username}</td>
              <td style={styles.td}>{person.address}</td>
              <td style={styles.td}>{person.age}</td>
              <td style={styles.td}>
                <button
                  style={styles.button}
                  onClick={() => handleEdit(person)}
                >
                  Edit
                </button>
                <button
                  style={{ ...styles.button, ...styles.deleteButton }}
                  onClick={() => handleDelete(person.id)}
                >
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

export default People;
