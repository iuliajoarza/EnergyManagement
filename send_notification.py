#!/usr/bin/env python3
"""
Quick script to send overconsumption notification for a specific user
Usage: python send_notification.py <username> [device_id] [consumption] [max]
"""

import json
import pika
import sys
from datetime import datetime

def send_notification(user_id, device_id="test-device", consumption=15.5, max_consumption=10.0):
    """Send overconsumption notification to specific user"""
    
    print(f"\n{'='*70}")
    print(f"   SENDING OVERCONSUMPTION NOTIFICATION")
    print(f"{'='*70}")
    print(f"   User ID: {user_id}")
    print(f"   Device: {device_id}")
    print(f"   Current: {consumption} kW")
    print(f"   Max: {max_consumption} kW")
    print(f"{'='*70}\n")
    
    try:
        connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
        channel = connection.channel()
        
        notification = {
            "user_id": user_id,
            "device_id": device_id,
            "consumption_value": consumption,
            "max_consumption": max_consumption,
            "type": "overconsumption",
            "message": f"⚠️ ALERT: Device {device_id} exceeded max consumption! Current: {consumption} kW (Max: {max_consumption} kW)",
            "timestamp": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        }
        
        channel.basic_publish(
            exchange='overconsumption.exchange',
            routing_key='overconsumption.alert',
            body=json.dumps(notification)
        )
        
        print("✅ SUCCESS! Notification sent to RabbitMQ")
        print(f"\n📱 Now check the UI:")
        print(f"   1. Open http://localhost in your browser")
        print(f"   2. Login as: {user_id}")
        print(f"   3. Click the Chat button (bottom right)")
        print(f"   4. You should see a system message with the alert!")
        print(f"\n💡 Tip: Open browser console (F12) to see WebSocket messages\n")
        
        connection.close()
        return True
        
    except Exception as e:
        print(f"❌ ERROR: {e}")
        print(f"\n⚠️  Make sure RabbitMQ is running:")
        print(f"   docker ps | grep rabbitmq\n")
        return False

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("\n❌ Usage: python send_notification.py <username> [device_id] [consumption] [max]")
        print("\nExample:")
        print("   python send_notification.py dana")
        print("   python send_notification.py dana my-device-123 20.5 15.0")
        print("\n💡 Tip: Use the username you're logged in with!\n")
        sys.exit(1)
    
    user = sys.argv[1]
    device = sys.argv[2] if len(sys.argv) > 2 else "test-device-001"
    consumption = float(sys.argv[3]) if len(sys.argv) > 3 else 15.5
    max_cons = float(sys.argv[4]) if len(sys.argv) > 4 else 10.0
    
    send_notification(user, device, consumption, max_cons)
