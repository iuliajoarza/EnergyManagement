import time
import json
import random
import pika
import os
import argparse
import psycopg2
from datetime import datetime, timedelta

RABBITMQ_HOST = 'rabbitmq'
RABBITMQ_QUEUE = 'energy_data'

COLLECTED_DEVICE_IDS = set()
DEVICE_IDS = []  # Populated from sync events
ACTIVE_DEVICE_ID = None  # Single active device
STOP_SIMULATION = False  # Set to True when device is deleted

SIM_DEVICE_ID = os.getenv('SIM_DEVICE_ID')  # optional env fallback
PG_HOST = os.getenv('PG_HOST', 'db2')
PG_DB = os.getenv('PG_DB', 'testdb2')
PG_USER = os.getenv('PG_USER', 'iulia')
PG_PASSWORD = os.getenv('PG_PASSWORD', 'iulia')

BASE_LOAD = random.uniform(0.5, 1.5)
simulated_time = datetime.utcnow()
MAX_BY_DEVICE = {}

def connect_rabbitmq(max_retries=10):
    retries = 0
    while retries < max_retries:
        try:
            connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST))
            channel = connection.channel()
            channel.queue_declare(queue=RABBITMQ_QUEUE)
            # Declare fanout exchange and simulator's own queue
            channel.exchange_declare(exchange='sync.events.exchange', exchange_type='fanout', durable=True)
            result = channel.queue_declare(queue='sync.events.simulator', durable=True)
            channel.queue_bind(exchange='sync.events.exchange', queue='sync.events.simulator')
            print(f"Successfully connected to RabbitMQ on attempt {retries + 1}")
            return channel

        except Exception as e:
            retries += 1
            print(f"RabbitMQ connection attempt {retries}/{max_retries} failed: {e}")
            if retries < max_retries:
                time.sleep(5)
            else:
                print("Max connection retries reached. Exiting.")
                raise

def get_max_consumption(device_id: str):
    try:
        conn = psycopg2.connect(host=PG_HOST, dbname=PG_DB, user=PG_USER, password=PG_PASSWORD)
        cur = conn.cursor()
        cur.execute("SELECT max_consumption FROM device WHERE id = %s", (device_id,))
        row = cur.fetchone()
        cur.close()
        conn.close()
        if row and row[0] is not None:
            return float(row[0])
        return None
    except Exception as e:
        print(f"[db] Error fetching max_consumption for {device_id}: {e}")
        return None

def get_max_cached(device_id: str):
    if device_id in MAX_BY_DEVICE:
        return MAX_BY_DEVICE[device_id]
    mc = get_max_consumption(device_id)
    MAX_BY_DEVICE[device_id] = mc
    if mc is not None:
        print(f"[db] Cached max_consumption for {device_id}: {mc}")
    return mc

def generate_measurement(device_id: str):
    global simulated_time
    hour = simulated_time.hour
    if 0 <= hour < 6:
        value = BASE_LOAD + random.uniform(-0.2, 0.2)
    elif 6 <= hour < 18:
        value = BASE_LOAD + random.uniform(0.1, 0.5)
    else:
        value = BASE_LOAD + random.uniform(0.3, 1.0)
    # Clamp to device max_consumption if available
    max_c = get_max_cached(device_id)
    if max_c is not None and value > max_c:
        value = max_c
    measurement = {
        "timestamp": simulated_time.isoformat() + 'Z',
        "device_id": device_id,
        "measurement_value": round(value, 2)
    }
    simulated_time += timedelta(minutes=10)
    return measurement

def start_sync_listener(channel):
    def on_message(ch, method, properties, body):
        try:
            evt = json.loads(body)
            event_type = evt.get('type')
            
            if event_type == 'device':
                device_id = evt.get('device_id')
                if device_id and device_id not in COLLECTED_DEVICE_IDS:
                    COLLECTED_DEVICE_IDS.add(device_id)
                    DEVICE_IDS.append(device_id)
                    print(f"[sync.events] Collected device_id: {device_id}")
            
            elif event_type == 'device_deleted':
                device_id = evt.get('device_id')
                if device_id:
                    if device_id in COLLECTED_DEVICE_IDS:
                        COLLECTED_DEVICE_IDS.remove(device_id)
                    if device_id in DEVICE_IDS:
                        DEVICE_IDS.remove(device_id)
                    print(f"[sync.events] Removed device_id: {device_id}")
                    # If running in single-device mode and this device gets deleted, signal stop
                    global ACTIVE_DEVICE_ID, STOP_SIMULATION
                    if ACTIVE_DEVICE_ID and device_id == ACTIVE_DEVICE_ID:
                        STOP_SIMULATION = True
                        print(f"[sync.events] Active device {device_id} deleted. Stopping simulation.")
                    
        except Exception as e:
            print(f"[sync.events] Error parsing event: {e}")

    channel.basic_consume(queue='sync.events.simulator', on_message_callback=on_message, auto_ack=True)

def parse_args():
    parser = argparse.ArgumentParser(description='Single device energy simulator')
    parser.add_argument('--device', help='Device ID to simulate (overrides SIM_DEVICE_ID env)')
    return parser.parse_args()

if __name__ == "__main__":
    args = parse_args()
    if args.device:
        ACTIVE_DEVICE_ID = args.device
    elif SIM_DEVICE_ID:
        ACTIVE_DEVICE_ID = SIM_DEVICE_ID
    if ACTIVE_DEVICE_ID:
        print(f"[startup] Fixed single device mode: {ACTIVE_DEVICE_ID}")
    print("Starting Device Data Simulator...")
    while True:
        try:
            channel = connect_rabbitmq()
            start_sync_listener(channel)
            idx = 0
            while True:
                # Pump sync events to collect device IDs
                channel.connection.process_data_events(time_limit=0.01)
                if STOP_SIMULATION:
                    print("[shutdown] Stop signal received due to device deletion. Exiting.")
                    raise SystemExit(0)
                if ACTIVE_DEVICE_ID:
                    measurement = generate_measurement(ACTIVE_DEVICE_ID)
                    channel.basic_publish(exchange='', routing_key=RABBITMQ_QUEUE, body=json.dumps(measurement))
                    print(f"Sent: {measurement}")
                    time.sleep(5)
                else:
                    if not DEVICE_IDS:
                        time.sleep(1)
                        continue
                    device_id = DEVICE_IDS[idx % len(DEVICE_IDS)]
                    idx += 1
                    measurement = generate_measurement(device_id)
                    channel.basic_publish(exchange='', routing_key=RABBITMQ_QUEUE, body=json.dumps(measurement))
                    print(f"Sent: {measurement}")
                    time.sleep(5)
        except Exception as e:
            print(f"Simulator encountered an error: {e}. Reconnecting...")
            time.sleep(5)
