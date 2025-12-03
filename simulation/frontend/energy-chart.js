import React, { useState } from 'react';
import { Line } from 'react-chartjs-2';
import 'chart.js/auto';

function EnergyChart() {
  const [data, setData] = useState({});
  const [deviceId, setDeviceId] = useState('');
  const [date, setDate] = useState('');

  const fetchData = async () => {
    const res = await fetch(`/api/consumption?deviceId=${deviceId}&day=${date}`);
    const result = await res.json();
    setData({
      labels: Object.keys(result).map(h => `${h}:00`),
      datasets: [{
        label: 'Energy (kWh)',
        data: Object.values(result),
        fill: false,
        borderColor: 'blue',
      }],
    });
  };

  return (
    <div>
      <h2>Energy Consumption Chart</h2>
      <input type="text" placeholder="Device ID" value={deviceId} onChange={e => setDeviceId(e.target.value)} />
      <input type="date" value={date} onChange={e => setDate(e.target.value)} />
      <button onClick={fetchData}>Show Chart</button>
      {data.labels && <Line data={data} />}
    </div>
  );
}

export default EnergyChart;
