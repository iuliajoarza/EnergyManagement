import React, { useState, useEffect, useRef } from 'react';
import websocketService from '../services/websocket';
import '../styles/Chat.css';

function Chat({ userId }) {
    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const [isConnected, setIsConnected] = useState(false);
    const [adminMode, setAdminMode] = useState(false);
    const messagesEndRef = useRef(null);

    // Connect once on mount, derive username from JWT if needed
    useEffect(() => {
        if (!isConnected) {
            connectWebSocket();
        }

        return () => {
            if (isConnected) {
                websocketService.disconnect();
            }
        };
    }, []);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    // Decode JWT to derive username if prop missing
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

    const getEffectiveUserId = () => {
        if (userId) return userId;
        const token = localStorage.getItem('token');
        const decoded = token ? decodeToken(token) : null;
        return decoded?.sub || decoded?.username || null;
    };

    const connectWebSocket = async () => {
        try {
            const uid = getEffectiveUserId();
            await websocketService.connect(
                uid,
                handleNotification,
                handleChatMessage
            );
            setIsConnected(true);
            console.log('WebSocket connected for user:', uid);
        } catch (error) {
            console.error('Failed to connect WebSocket:', error);
            setIsConnected(false);
        }
    };

    const handleNotification = (notification) => {
        // Send energy alert to notification center instead of chat
        if (window.notificationCenter) {
            window.notificationCenter.addNotification({
                message: `⚠️ ${notification.message}`,
                type: 'alert',
                timestamp: notification.timestamp
            });
        }
    };

    const handleChatMessage = (chatMessage) => {
        setMessages(prev => [...prev, chatMessage]);
    };

    const sendMessage = () => {
        if (inputMessage.trim() && isConnected) {
            const uid = getEffectiveUserId();
            const userMessage = {
                username: uid,
                user_id: uid,
                message: inputMessage,
                sender: 'user',
                timestamp: new Date().toISOString()
            };

            // Add user message to UI immediately
            setMessages(prev => [...prev, userMessage]);

            // Send to appropriate destination based on mode
            if (adminMode) {
                // Send directly to admin
                websocketService.requestAdmin(uid, inputMessage, `session-${Date.now()}`);
            } else {
                // Send to chatbot
                websocketService.sendChatMessage(uid, inputMessage, 'default');
            }

            setInputMessage('');
        }
    };

    const toggleAdminMode = () => {
        setAdminMode(!adminMode);
        
        if (!adminMode) {
            // Switching to admin mode
            const confirmMessage = {
                sender: 'system',
                message: '👤 Admin mode activated. Your messages will be sent to a human representative.',
                timestamp: new Date().toISOString()
            };
            setMessages(prev => [...prev, confirmMessage]);
        } else {
            // Switching back to bot mode
            const confirmMessage = {
                sender: 'system',
                message: '🤖 Bot mode activated. Your messages will be handled by the automated assistant.',
                timestamp: new Date().toISOString()
            };
            setMessages(prev => [...prev, confirmMessage]);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const toggleChat = () => {
        setIsOpen(!isOpen);
    };

    const getMessageClass = (sender, type) => {
        let baseClass = '';
        switch (sender) {
            case 'user':
                baseClass = 'message-user';
                break;
            case 'bot':
                baseClass = 'message-bot';
                break;
            case 'system':
                baseClass = 'message-system';
                break;
            case 'admin':
                baseClass = 'message-admin';
                break;
            default:
                baseClass = 'message-bot';
        }
        
        // Add alert class if it's an energy alert
        if (type === 'alert') {
            baseClass += ' message-alert';
        }
        
        return baseClass;
    };

    const formatTime = (timestamp) => {
        if (!timestamp) return '';
        try {
            const date = new Date(timestamp);
            return date.toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' });
        } catch {
            return '';
        }
    };

    return (
        <>
            {/* Chat Toggle Button */}
            <div className={`chat-toggle ${isOpen ? 'hidden' : ''}`} onClick={toggleChat}>
                <span className="chat-icon">💬</span>
                {!isConnected && <span className="connection-indicator">⚠️</span>}
            </div>

            {/* Chat Window */}
            {isOpen && (
                <div className="chat-window">
                    <div className="chat-header">
                        <div className="chat-header-content">
                            <h3>Customer Support {adminMode && '- Admin Mode'}</h3>
                            <button 
                                className={`admin-request-btn ${adminMode ? 'active' : ''}`}
                                onClick={toggleAdminMode} 
                                title={adminMode ? "Switch to Bot" : "Talk to Admin"}
                            >
                                {adminMode ? '🤖 Bot' : '👤 Admin'}
                            </button>
                        </div>
                        <button className="chat-close" onClick={toggleChat}>×</button>
                    </div>

                    <div className="chat-messages">
                        {messages.length === 0 && (
                            <div className="welcome-message">
                                <p>👋 Bună! Sunt asistentul virtual.</p>
                                <p>Cum te pot ajuta?</p>
                            </div>
                        )}
                        {messages.map((msg, index) => (
                            <div key={index} className={`message ${getMessageClass(msg.sender, msg.type)}`}>
                                <div className="message-content">{msg.message}</div>
                                <div className="message-time">{formatTime(msg.timestamp)}</div>
                            </div>
                        ))}
                        <div ref={messagesEndRef} />
                    </div>

                    <div className="chat-input-container">
                        <input
                            type="text"
                            className="chat-input"
                            placeholder={isConnected ? (adminMode ? "Message to admin..." : "Scrie mesajul tău...") : "Connecting..."}
                            value={inputMessage}
                            onChange={(e) => setInputMessage(e.target.value)}
                            onKeyPress={handleKeyPress}
                            disabled={!isConnected}
                        />
                        <button
                            className="chat-send-button"
                            onClick={sendMessage}
                            disabled={!isConnected || !inputMessage.trim()}
                        >
                            ➤
                        </button>
                    </div>

                    {!isConnected && (
                        <div className="connection-status">
                            Reconnecting...
                        </div>
                    )}
                </div>
            )}
        </>
    );
}

export default Chat;
