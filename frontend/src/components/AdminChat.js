import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import websocketService from '../services/websocket';
import './AdminChat.css';

const AdminChat = () => {
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [activeUser, setActiveUser] = useState(null);
  const [replyText, setReplyText] = useState('');
  const [userConversations, setUserConversations] = useState(() => {
    // Load conversations from localStorage on mount
    const saved = localStorage.getItem('adminConversations');
    return saved ? JSON.parse(saved) : {};
  });
  const [isConnected, setIsConnected] = useState(false);

  // Save conversations to localStorage whenever they change
  useEffect(() => {
    localStorage.setItem('adminConversations', JSON.stringify(userConversations));
  }, [userConversations]);

  // Decode JWT subject (username) as fallback
  const decodeToken = (token) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''));
      return JSON.parse(jsonPayload);
    } catch {
      return null;
    }
  };

  const getAdminUsername = () => {
    if (user && user.username) return user.username;
    const token = user?.token || localStorage.getItem('token');
    const decoded = token ? decodeToken(token) : null;
    return decoded?.sub || decoded?.username || null;
  };

  useEffect(() => {
    // Subscribe to admin messages (doesn't matter if connected yet, will work when it connects)
    const subscription = websocketService.subscribeToAdminMessages((message) => {
      console.log('[AdminChat] Received user message:', message);
      
      setUserConversations(prev => {
        const username = message.username || message.user_id || message.userId;
        
        if (!username || username === 'undefined') {
          console.warn('[AdminChat] Invalid username, skipping message:', username);
          return prev;
        }
        
        const existing = prev[username] || [];
        return {
          ...prev,
          [username]: [...existing, { ...message, sender: 'user' }]
        };
      });
    });

    // Check connection status every second
    const statusCheckInterval = setInterval(() => {
      setIsConnected(websocketService.isConnected());
    }, 1000);

    return () => {
      clearInterval(statusCheckInterval);
      // WebSocket stays connected globally
    };
  }, [user?.username]);

  // Set initial connection status
  useEffect(() => {
    setIsConnected(websocketService.isConnected());
  }, []);

  const handleUserSelect = (username) => {
    setActiveUser(username);
  };

  const handleSendReply = () => {
    // Validate that we have a selected user and message
    if (!replyText.trim()) {
      console.warn('Cannot send empty reply');
      return;
    }
    
    if (!activeUser || activeUser === 'undefined') {
      console.warn('Must select a user before sending reply');
      alert('Please select a user from the conversation list first');
      return;
    }

    const replyMessage = {
      userId: activeUser,
      username: activeUser,
      message: replyText,
      sender: 'admin',
      timestamp: new Date().toISOString(),
      sessionId: `admin-${Date.now()}`
    };

    console.log('Sending admin reply:', replyMessage);

    // Send via WebSocket
    websocketService.sendAdminReply(replyMessage);

    // Add to local state
    setUserConversations(prev => ({
      ...prev,
      [activeUser]: [...(prev[activeUser] || []), { ...replyMessage, sender: 'admin' }]
    }));

    setReplyText('');
  };

  const activeMessages = activeUser ? userConversations[activeUser] || [] : [];

  return (
    <div className="admin-chat-container">
      <div className="admin-header">
        <h2>Admin Chat Support</h2>
        <div className="admin-info">
          <span className="admin-badge">Admin: {user?.username || user?.id}</span>
          <span className={`connection-status ${isConnected ? 'connected' : 'disconnected'}`}>
            {isConnected ? '● Connected' : '○ Disconnected'}
          </span>
        </div>
      </div>

      <div className="admin-layout">
        {/* User list sidebar */}
        <div className="user-list">
          <h3>Active Conversations</h3>
          {Object.keys(userConversations).length === 0 ? (
            <p className="no-users">No active conversations</p>
          ) : (
            Object.keys(userConversations).map(userId => (
              <div
                key={userId}
                className={`user-item ${activeUser === userId ? 'active' : ''}`}
                onClick={() => handleUserSelect(userId)}
              >
                <div className="user-icon">👤</div>
                <div className="user-info">
                  <div className="user-name">{userId}</div>
                  <div className="user-preview">
                    {userConversations[userId][userConversations[userId].length - 1]?.message.substring(0, 30)}...
                  </div>
                </div>
                <div className="unread-badge">{userConversations[userId].length}</div>
              </div>
            ))
          )}
        </div>

        {/* Chat area */}
        <div className="chat-area">
          {!activeUser ? (
            <div className="no-selection">
              <p>Select a user to view conversation</p>
            </div>
          ) : (
            <>
              <div className="chat-header">
                <h3>Chat with {activeUser}</h3>
              </div>

              <div className="messages-container">
                {activeMessages.map((msg, idx) => (
                  <div key={idx} className={`message ${msg.sender}`}>
                    <div className="message-header">
                      <span className="sender-name">
                        {msg.sender === 'user' ? activeUser : 'You (Admin)'}
                      </span>
                      <span className="message-time">
                        {new Date(msg.timestamp).toLocaleTimeString()}
                      </span>
                    </div>
                    <div className="message-body">{msg.message}</div>
                  </div>
                ))}
              </div>

              <div className="reply-box">
                <input
                  type="text"
                  value={replyText}
                  onChange={(e) => setReplyText(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSendReply()}
                  placeholder="Type your reply..."
                  className="reply-input"
                />
                <button onClick={handleSendReply} className="send-button">
                  Send ➤
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminChat;
