import React, { useState, useCallback } from 'react';
import '../styles/NotificationCenter.css';

function NotificationCenter() {
    const [notifications, setNotifications] = useState([]);

    // Function to add notification - exposed via window global
    const addNotification = useCallback((notification) => {
        const id = Date.now();
        const newNotification = {
            id,
            message: notification.message,
            type: notification.type || 'alert',
            timestamp: notification.timestamp
        };
        
        setNotifications(prev => [...prev, newNotification]);
        
        // Auto-remove after 8 seconds
        setTimeout(() => {
            setNotifications(prev => prev.filter(n => n.id !== id));
        }, 8000);
    }, []);

    // Expose to global so Chat component can use it
    React.useEffect(() => {
        window.notificationCenter = { addNotification };
    }, [addNotification]);

    const removeNotification = (id) => {
        setNotifications(prev => prev.filter(n => n.id !== id));
    };

    return (
        <div className="notification-center">
            {notifications.map((notification) => (
                <div
                    key={notification.id}
                    className={`notification notification-${notification.type}`}
                >
                    <div className="notification-content">
                        <div className="notification-message">
                            {notification.message}
                        </div>
                    </div>
                    <button
                        className="notification-close"
                        onClick={() => removeNotification(notification.id)}
                    >
                        ✕
                    </button>
                </div>
            ))}
        </div>
    );
}

export default NotificationCenter;
