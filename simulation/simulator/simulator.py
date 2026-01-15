import time
import json
import random
import pika
import os
import argparse
from datetime import datetime, timedelta

RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'rabbitmq')
RABBITMQ_QUEUE = 'energy_data'

COLLECTED_DEVICE_IDS = set()
DEVICE_IDS = []  # Populated from sync events
ACTIVE_DEVICE_ID = None  # Single active device
STOP_SIMULATION = False  # Set to True when device is deleted

SIM_DEVICE_ID = os.getenv('SIM_DEVICE_ID')  # optional env fallback

BASE_LOAD = random.uniform(0.8, 1.8)
simulated_time = datetime.utcnow()
MAX_BY_DEVICE = {}  # Cache: device_id -> max_consumption from sync events

def connect_rabbitmq(max_retries=10):
    retries = 0
    while retries < max_retries:
        try:
            print(f"Attempting to connect to RabbitMQ at {RABBITMQ_HOST}...")
            connection = pika.BlockingConnection(pika.ConnectionParameters(
                host=RABBITMQ_HOST,
                connection_attempts=3,
                retry_delay=2,
                socket_timeout=10
            ))
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

def get_max_cached(device_id: str):
    """Get cached max_consumption from sync event attributes."""
    return MAX_BY_DEVICE.get(device_id)

def generate_measurement(device_id: str):
    global simulated_time
    hour = simulated_time.hour
    if 0 <= hour < 6:
        value = BASE_LOAD + random.uniform(-0.2, 0.2)
    elif 6 <= hour < 18:
        value = BASE_LOAD + random.uniform(0.1, 0.5)
    else:
        value = BASE_LOAD + random.uniform(0.3, 1.0)
    # Don't clamp - allow overconsumption to trigger alerts!
    # max_c = get_max_cached(device_id)
    # if max_c is not None and value > max_c:
    #     value = max_c
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
                attributes = evt.get('attributes', {})
                
                # Cache max_consumption from sync event
                max_cons = attributes.get('max_consumption')
                if max_cons is not None:
                    MAX_BY_DEVICE[device_id] = float(max_cons)
                    print(f"[sync.events] Cached max_consumption for {device_id}: {max_cons}")
                
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
                    if device_id in MAX_BY_DEVICE:
                        del MAX_BY_DEVICE[device_id]
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

def publish_sync_event(channel, device_id, user_id, username, max_consumption):
    """Publish a sync event and directly update monitoring database"""
    try:
        sync_event = {
            "type": "device",
            "device_id": device_id,
            "attributes": {
                "user_id": user_id,
                "username": username,
                "max_consumption": max_consumption
            }
        }
        # Publish to sync exchange so monitoring service picks it up
        channel.exchange_declare(exchange='sync.events.exchange', exchange_type='fanout', durable=True)
        channel.basic_publish(
            exchange='sync.events.exchange',
            routing_key='',
            body=json.dumps(sync_event)
        )
        print(f"[sync] Published device sync: {device_id} for user {username} (max: {max_consumption} kW)")
        
        # Also directly update monitoring database to ensure username is saved
        import subprocess
        update_cmd = [
            'docker', 'exec', 'monitoring-db', 'psql', '-U', 'iulia', '-d', 'monitoring_db',
            '-c',
            f"INSERT INTO device_info (device_id, user_id, username, max_consumption) VALUES ('{device_id}', '{user_id}', '{username}', {max_consumption}) ON CONFLICT (device_id) DO UPDATE SET username = EXCLUDED.username, user_id = EXCLUDED.user_id, max_consumption = EXCLUDED.max_consumption;"
        ]
        result = subprocess.run(update_cmd, capture_output=True, text=True, timeout=10)
        if result.returncode == 0:
            print(f"[sync] Updated monitoring database with device info")
        else:
            print(f"[sync] Warning: Could not update monitoring database: {result.stderr}")
        
        return True
    except Exception as e:
        print(f"[sync] Error publishing sync event: {e}")
        return False

def get_device_info(device_id: str):
    """Query device database to get user_id, username and max_consumption"""
    import subprocess
    try:
        # Query device info from db2 (device database)
        cmd = [
            'docker', 'exec', 'db2', 'psql', '-U', 'iulia', '-d', 'testdb2',
            '-t', '-c',
            f"SELECT user_id, max_consumption FROM device WHERE id = '{device_id}' LIMIT 1;"
        ]
        
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=10)
        
        if result.returncode == 0:
            output = result.stdout.strip()
            if output and '|' in output:
                parts = output.split('|')
                if len(parts) >= 2:
                    user_id = parts[0].strip()
                    max_cons_str = parts[1].strip()
                    if user_id and max_cons_str:
                        max_cons = float(max_cons_str)
                        # Now get username from user_cache
                        username = get_username_from_id(user_id)
                        print(f"[startup] Found device {device_id}: user_id={user_id}, username={username}, max_consumption={max_cons} kW")
                        return user_id, username, max_cons
        else:
            print(f"[startup] Query failed: {result.stderr.strip()}")
        return None, None, None
    except Exception as e:
        print(f"[startup] Error querying device info: {e}")
        return None, None, None

def get_username_from_id(user_id: str):
    """Query user_cache to get username from user_id"""
    import subprocess
    try:
        cmd = [
            'docker', 'exec', 'db2', 'psql', '-U', 'iulia', '-d', 'testdb2',
            '-t', '-c',
            f"SELECT username FROM user_cache WHERE user_id = '{user_id}' LIMIT 1;"
        ]
        
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=10)
        
        if result.returncode == 0:
            username = result.stdout.strip()
            if username:
                return username
        return user_id  # Fallback to user_id if username not found
    except Exception as e:
        print(f"[startup] Warning: Could not get username: {e}")
        return user_id  # Fallback to user_id if query fails

if __name__ == "__main__":
    args = parse_args()
    if args.device:
        ACTIVE_DEVICE_ID = args.device
    elif SIM_DEVICE_ID:
        ACTIVE_DEVICE_ID = SIM_DEVICE_ID
    
    if ACTIVE_DEVICE_ID:
        print(f"\n[startup] Starting simulator for device: {ACTIVE_DEVICE_ID}")
    
    print("Starting Device Data Simulator...\n")
    
    while True:
        try:
            channel = connect_rabbitmq()
            start_sync_listener(channel)
            
            # If running in single-device mode, sync device info automatically
            if ACTIVE_DEVICE_ID:
                print(f"[startup] Syncing device info...")
                user_id, username, max_consumption = get_device_info(ACTIVE_DEVICE_ID)
                
                if user_id is not None and username is not None and max_consumption is not None:
                    # Publish sync event to notify monitoring service
                    if publish_sync_event(channel, ACTIVE_DEVICE_ID, user_id, username, max_consumption):
                        MAX_BY_DEVICE[ACTIVE_DEVICE_ID] = max_consumption
                        print(f"[startup] Device info synced successfully\n")
                        # Give monitoring service time to process
                        time.sleep(2)
                    else:
                        print(f"[startup] Failed to publish sync event\n")
                else:
                    print(f"[startup] ERROR: Could not get device info. Check if device {ACTIVE_DEVICE_ID} exists in database.\n")
            
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
