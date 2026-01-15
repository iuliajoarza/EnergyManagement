#!/usr/bin/env python3
"""Send device sync event to monitoring service"""

import json
import pika

RABBITMQ_HOST = 'localhost'

def send_device_sync(device_id, username, max_consumption):
    """Send device sync event to monitoring service"""
    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST))
    channel = connection.channel()
    
    # Sync event message
    sync_event = {
        "type": "device",
        "device_id": device_id,
        "attributes": {
            "username": username,
            "max_consumption": max_consumption
        }
    }
    
    # Send to sync.events.exchange fanout exchange
    channel.exchange_declare(exchange='sync.events.exchange', exchange_type='fanout', durable=True)
    channel.basic_publish(
        exchange='sync.events.exchange',
        routing_key='',
        body=json.dumps(sync_event)
    )
    
    print(f"✅ Sent sync event for device {device_id}")
    print(f"   Username: {username}, Max consumption: {max_consumption} kW")
    
    connection.close()

if __name__ == '__main__':
    # Send sync for device b
    send_device_sync(
        device_id="05a79315-9c8e-4aa8-811b-0b8f571e22d8",
        username="dana",
        max_consumption=0.5
    )
