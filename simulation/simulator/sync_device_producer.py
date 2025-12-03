import pika
import json

RABBITMQ_HOST = 'rabbitmq'
SYNC_QUEUE = 'sync.events'

device_event = {
    'type': 'device',
    'device_id': 'device_456',
    'attributes': {'name': 'Test Device'}
}

def send_sync_event(event):
    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST))
    channel = connection.channel()
    channel.queue_declare(queue=SYNC_QUEUE, durable=True)
    channel.basic_publish(exchange='', routing_key=SYNC_QUEUE, body=json.dumps(event))
    print(f"Sent sync event: {event}")
    connection.close()

if __name__ == "__main__":
    send_sync_event(device_event)
