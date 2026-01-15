import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = {};
    }

    connect(userId, onNotification, onChatMessage) {
        return new Promise((resolve, reject) => {
            // Connect directly to WebSocket service on exposed port 8085
            const wsUrl = 'http://localhost:8085/ws';
            
            console.log('[WebSocket] Connecting to:', wsUrl);
            console.log('[WebSocket] Current hostname:', window.location.hostname);
            console.log('[WebSocket] Current origin:', window.location.origin);
            
            // Add timeout - if not connected in 10 seconds, reject
            const timeoutId = setTimeout(() => {
                console.error('[WebSocket] Connection timeout after 10 seconds');
                reject(new Error('WebSocket connection timeout'));
            }, 10000);
            
            const client = new Client({
                webSocketFactory: () => {
                    console.log('[WebSocket] Creating SockJS connection...');
                    return new SockJS(wsUrl);
                },
                debug: (str) => {
                    console.log('[WebSocket STOMP]', str);
                },
                reconnectDelay: 5000,
                connectHeaders: {
                    'userId': userId || 'anonymous'
                },
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });

            client.onConnect = (frame) => {
                clearTimeout(timeoutId);
                console.log('WebSocket Connected:', frame);
                this.connected = true;
                this.stompClient = client;

                // Subscribe to user-specific notifications
                if (userId) {
                    this.subscriptions.notifications = client.subscribe(
                        `/topic/notifications/${userId}`,
                        (message) => {
                            const notification = JSON.parse(message.body);
                            console.log('Received notification:', notification);
                            if (onNotification) {
                                onNotification(notification);
                            }
                        }
                    );

                    // Subscribe to chat messages for this user
                    this.subscriptions.chat = client.subscribe(
                        `/topic/chat/${userId}`,
                        (message) => {
                            const chatMsg = JSON.parse(message.body);
                            console.log('Received chat message:', chatMsg);
                            if (onChatMessage) {
                                onChatMessage(chatMsg);
                            }
                        }
                    );
                }

                // Subscribe to bot responses from chat queue
                this.subscriptions.botResponses = client.subscribe(
                    `/queue/chat.bot.responses`,
                    (message) => {
                        const chatMsg = JSON.parse(message.body);
                        console.log('Received bot response:', chatMsg);
                        if (onChatMessage) {
                            onChatMessage(chatMsg);
                        }
                    }
                );

                resolve();
            };

            client.onStompError = (frame) => {
                clearTimeout(timeoutId);
                console.error('WebSocket connection error:', frame);
                this.connected = false;
                reject(frame);
            };

            client.onWebSocketError = (error) => {
                clearTimeout(timeoutId);
                console.error('WebSocket error:', error);
                this.connected = false;
                reject(error);
            };

            client.onDisconnect = () => {
                console.log('WebSocket disconnected');
                this.connected = false;
            };

            client.activate();
        });
    }

    disconnect() {
        if (this.stompClient && this.connected) {
            // Unsubscribe from all
            Object.values(this.subscriptions).forEach(sub => {
                if (sub) sub.unsubscribe();
            });
            this.stompClient.deactivate();
            this.connected = false;
            console.log('WebSocket Disconnected');
        }
    }

    sendChatMessage(userId, message, sessionId) {
        if (this.stompClient && this.connected) {
            const chatMessage = {
                user_id: userId,
                message: message,
                timestamp: new Date().toISOString(),
                sender: 'user',
                session_id: sessionId || 'default'
            };
            this.stompClient.publish({
                destination: '/app/chat.send',
                body: JSON.stringify(chatMessage)
            });
        } else {
            console.error('WebSocket not connected');
        }
    }

    requestAdmin(userId, message, sessionId) {
        if (this.stompClient && this.connected) {
            // Use snake_case to match backend JSON properties
            const adminRequest = {
                user_id: userId,
                username: userId,
                message: message,
                timestamp: new Date().toISOString(),
                sender: 'user',
                session_id: sessionId || `session-${Date.now()}`
            };
            this.stompClient.publish({
                destination: '/app/chat.request.admin',
                body: JSON.stringify(adminRequest)
            });
            console.log('Admin request sent:', adminRequest);
        } else {
            console.error('WebSocket not connected');
        }
    }

    sendAdminReply(message) {
        if (this.stompClient && this.connected) {
            // Map to snake_case for backend compatibility
            const adminReply = {
                user_id: message.userId || message.username,
                username: message.username || message.userId,
                message: message.message,
                timestamp: new Date().toISOString(),
                sender: 'admin',
                session_id: message.sessionId || `admin-${Date.now()}`
            };
            this.stompClient.publish({
                destination: '/app/chat.admin.reply',
                body: JSON.stringify(adminReply)
            });
            console.log('Admin reply sent:', adminReply);
        } else {
            console.error('WebSocket not connected');
        }
    }

    subscribeToAdminMessages(callback) {
        if (this.stompClient && this.connected) {
            const subscription = this.stompClient.subscribe(
                '/topic/admin/messages',
                (message) => {
                    const adminMsg = JSON.parse(message.body);
                    console.log('Admin received message:', adminMsg);
                    if (callback) {
                        callback(adminMsg);
                    }
                }
            );
            this.subscriptions.adminMessages = subscription;
            return subscription;
        } else {
            console.error('WebSocket not connected');
            return null;
        }
    }

    isConnected() {
        return this.connected;
    }
}

export default new WebSocketService();
