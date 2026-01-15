#!/usr/bin/env python3
"""Send manual sync event for device to monitoring service"""

import json
import pika

RABBITMQ_HOST = 'localhost'

def send_device_sync(device_id, user_id, max_consumption):
    """Send device sync event to monitoring service"""
    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST))
    channel = connection.channel()
    
    # Sync event message
    sync_event = {
        "type": "device",
        "device_id": device_id,
        "attributes": {
            "user_id": user_id,
            "max_consumption": max_consumption,
            "address": "Lab",
            "description": "Test device for overconsumption"
        }
    }
    
    # Send to sync.events.exchange fanout exchange
    channel.exchange_declare(exchange='sync.events.exchange', exchange_type='fanout', durable=True)
    channel.basic_publish(
        exchange='sync.events.exchange',
        routing_key='',  # Fanout doesn't use routing key
        body=json.dumps(sync_event)
    )
    
    print(f"✅ Sent sync event for device {device_id}")
    print(f"   User: {user_id}, Max consumption: {max_consumption} kW")
    
    connection.close()

if __name__ == '__main__':
    # Send sync for dana's device
    send_device_sync(
        device_id="913589bc-0003-4d5e-979d-fa566b6652d2",
        user_id="dana",
        max_consumption=0.7
    )
