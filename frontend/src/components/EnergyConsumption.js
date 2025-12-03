import React, { useState, useEffect } from 'react';
import { devicesAPI } from '../services/api';
import axios from 'axios';

const EnergyConsumption = () => {
  const [devices, setDevices] = useState([]);
  const [selectedDevice, setSelectedDevice] = useState('');
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [hourlyData, setHourlyData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [chartType, setChartType] = useState('bar'); // 'bar' or 'line'

  useEffect(() => {
    fetchUserDevices();
  }, []);

  const fetchUserDevices = async () => {
    try {
      // Backend filters devices by role automatically
      const response = await devicesAPI.getAll();
      setDevices(Array.isArray(response.data) ? response.data : []);
      if (response.data.length > 0) {
        setSelectedDevice(response.data[0].id);
      }
    } catch (err) {
      setError('Failed to fetch your devices');
      console.error(err);
    }
  };

  const fetchHourlyData = async () => {
    if (!selectedDevice) return;
    
    setLoading(true);
    setError('');
    try {
      const response = await axios.get(
        `http://localhost/api/monitoring/energy/hourly/${selectedDevice}?date=${selectedDate}`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      setHourlyData(response.data);
    } catch (err) {
      setError('Failed to fetch energy consumption data');
      setHourlyData([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedDevice) {
      fetchHourlyData();
    }
  }, [selectedDevice, selectedDate]);

  const getMaxValue = () => {
    if (hourlyData.length === 0) return 2;
    return Math.ceil(Math.max(...hourlyData.map(d => d.totalEnergyKwh)) * 1.2);
  };

  const formatHour = (timestamp) => {
    const date = new Date(timestamp);
    return `${date.getHours()}:00`;
  };

  const renderBarChart = () => {
    const maxValue = getMaxValue();
    const barWidth = 100 / 24;

    return (
      <div style={{ position: 'relative', height: '400px', border: '1px solid #ddd', borderRadius: '4px', padding: '20px', background: '#f9f9f9' }}>
        <div style={{ position: 'relative', height: '100%', display: 'flex', alignItems: 'flex-end', justifyContent: 'space-around' }}>
          {Array.from({ length: 24 }, (_, i) => {
            const dataPoint = hourlyData.find(d => new Date(d.hourTimestamp).getHours() === i);
            const value = dataPoint ? dataPoint.totalEnergyKwh : 0;
            const heightPercent = (value / maxValue) * 100;

            return (
              <div key={i} style={{ flex: '1', display: 'flex', flexDirection: 'column', alignItems: 'center', marginRight: '2px' }}>
                <div
                  style={{
                    width: '100%',
                    height: `${heightPercent}%`,
                    backgroundColor: value > 0 ? '#4CAF50' : '#e0e0e0',
                    borderRadius: '4px 4px 0 0',
                    transition: 'all 0.3s ease',
                    position: 'relative'
                  }}
                  title={`${i}:00 - ${value.toFixed(2)} kWh`}
                >
                  {value > 0 && (
                    <span style={{ position: 'absolute', top: '-20px', fontSize: '10px', width: '100%', textAlign: 'center' }}>
                      {value.toFixed(2)}
                    </span>
                  )}
                </div>
                <span style={{ fontSize: '10px', marginTop: '5px' }}>{i}</span>
              </div>
            );
          })}
        </div>
        <div style={{ marginTop: '10px', textAlign: 'center', fontSize: '12px', color: '#666' }}>
          Hours (0-23)
        </div>
        <div style={{ position: 'absolute', left: '0', top: '50%', transform: 'rotate(-90deg) translateX(-50%)', transformOrigin: 'left', fontSize: '12px', color: '#666' }}>
          Energy (kWh)
        </div>
      </div>
    );
  };

  const renderLineChart = () => {
    const maxValue = getMaxValue();
    const width = 800;
    const height = 400;
    const padding = 40;
    const chartWidth = width - 2 * padding;
    const chartHeight = height - 2 * padding;

    const points = Array.from({ length: 24 }, (_, i) => {
      const dataPoint = hourlyData.find(d => new Date(d.hourTimestamp).getHours() === i);
      const value = dataPoint ? dataPoint.totalEnergyKwh : 0;
      const x = padding + (i / 23) * chartWidth;
      const y = height - padding - (value / maxValue) * chartHeight;
      return { x, y, value, hour: i };
    });

    const pathData = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');

    return (
      <svg width={width} height={height} style={{ border: '1px solid #ddd', borderRadius: '4px', background: '#f9f9f9' }}>
        {/* Y-axis */}
        <line x1={padding} y1={padding} x2={padding} y2={height - padding} stroke="#666" strokeWidth="2" />
        {/* X-axis */}
        <line x1={padding} y1={height - padding} x2={width - padding} y2={height - padding} stroke="#666" strokeWidth="2" />
        
        {/* Grid lines */}
        {[0, 1, 2, 3, 4].map(i => (
          <g key={i}>
            <line
              x1={padding}
              y1={padding + (i / 4) * chartHeight}
              x2={width - padding}
              y2={padding + (i / 4) * chartHeight}
              stroke="#ddd"
              strokeWidth="1"
            />
            <text x={padding - 30} y={padding + (i / 4) * chartHeight + 5} fontSize="10" fill="#666">
              {(maxValue * (4 - i) / 4).toFixed(1)}
            </text>
          </g>
        ))}

        {/* Line chart */}
        <path d={pathData} fill="none" stroke="#4CAF50" strokeWidth="2" />

        {/* Data points */}
        {points.map((p, i) => (
          <circle key={i} cx={p.x} cy={p.y} r="4" fill="#4CAF50">
            <title>{`${p.hour}:00 - ${p.value.toFixed(2)} kWh`}</title>
          </circle>
        ))}

        {/* X-axis labels */}
        {[0, 6, 12, 18, 23].map(hour => {
          const x = padding + (hour / 23) * chartWidth;
          return (
            <text key={hour} x={x} y={height - padding + 20} fontSize="10" textAnchor="middle" fill="#666">
              {hour}:00
            </text>
          );
        })}

        {/* Axis labels */}
        <text x={width / 2} y={height - 5} fontSize="12" textAnchor="middle" fill="#666">
          Hours
        </text>
        <text x={15} y={height / 2} fontSize="12" textAnchor="middle" fill="#666" transform={`rotate(-90 15 ${height / 2})`}>
          Energy (kWh)
        </text>
      </svg>
    );
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Energy Consumption</h2>

      <div style={{ marginBottom: '20px', display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap' }}>
        <div>
          <label style={{ marginRight: '10px' }}>Device:</label>
          <select
            value={selectedDevice}
            onChange={(e) => setSelectedDevice(e.target.value)}
            style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
          >
            {devices.map(device => (
              <option key={device.id} value={device.id}>{device.name}</option>
            ))}
          </select>
        </div>

        <div>
          <label style={{ marginRight: '10px' }}>Date:</label>
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
          />
        </div>

        <div>
          <label style={{ marginRight: '10px' }}>Chart Type:</label>
          <select
            value={chartType}
            onChange={(e) => setChartType(e.target.value)}
            style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
          >
            <option value="bar">Bar Chart</option>
            <option value="line">Line Chart</option>
          </select>
        </div>

        <button
          onClick={fetchHourlyData}
          style={{
            padding: '8px 16px',
            backgroundColor: '#4CAF50',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Refresh
        </button>
      </div>

      {error && <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>}

      {loading ? (
        <div>Loading...</div>
      ) : hourlyData.length === 0 ? (
        <div style={{ 
          padding: '40px', 
          textAlign: 'center', 
          backgroundColor: '#fff3cd', 
          border: '1px solid #ffc107', 
          borderRadius: '8px',
          color: '#856404',
          fontSize: '16px',
          fontWeight: '500'
        }}>
          ⚠️ Device not in use - Simulation is not started
        </div>
      ) : (
        <div>
          <div style={{ marginBottom: '10px', fontSize: '14px', color: '#666' }}>
            Total: {hourlyData.reduce((sum, d) => sum + d.totalEnergyKwh, 0).toFixed(2)} kWh
          </div>
          {chartType === 'bar' ? renderBarChart() : renderLineChart()}
        </div>
      )}
    </div>
  );
};

export default EnergyConsumption;
