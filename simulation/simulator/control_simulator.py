#!/usr/bin/env python3
import argparse
import json
import pika
import sys

RABBITMQ_HOST = 'rabbitmq'
CONTROL_QUEUE = 'simulation.control'

def publish(action: str, devices: list[str]):
    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST))
    channel = connection.channel()
    channel.queue_declare(queue=CONTROL_QUEUE, durable=False)
    payload = {"action": action}
    if devices:
        if len(devices) == 1:
            payload["device_id"] = devices[0]
        else:
            payload["device_ids"] = devices
    channel.basic_publish(exchange='', routing_key=CONTROL_QUEUE, body=json.dumps(payload))
    print(f"[control] Sent: {payload}")
    connection.close()

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Control simulator active device.')
    parser.add_argument('--device', action='append', help='Device ID (repeat flag for multiple).')
    parser.add_argument('--devices', help='Comma separated list of device IDs.')
    parser.add_argument('--add', action='store_true', help='Add devices to active set.')
    parser.add_argument('--remove', action='store_true', help='Remove devices from active set.')
    parser.add_argument('--clear', action='store_true', help='Clear all active devices.')
    parser.add_argument('--set', action='store_true', help='Replace active devices with given list.')
    parser.add_argument('--start', action='store_true', help='Alias for --set.')
    parser.add_argument('--stop', action='store_true', help='Alias for --remove with provided devices.')
    args = parser.parse_args()

    devices = []
    if args.device:
        devices.extend(args.device)
    if args.devices:
        devices.extend([d.strip() for d in args.devices.split(',') if d.strip()])

    action = 'set'
    if args.add:
        action = 'add'
    elif args.remove or args.stop:
        action = 'remove'
    elif args.clear:
        action = 'clear'
    elif args.start:
        action = 'start'
    elif args.set:
        action = 'set'

    if action in ('set','start','add') and not devices:
        print('Eroare: trebuie sa specifici minim un device cu --device sau --devices.')
        sys.exit(1)

    publish(action, devices)
