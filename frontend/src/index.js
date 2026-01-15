import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

// Disable browser notifications completely
if ('Notification' in window) {
    window.Notification = null;
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
