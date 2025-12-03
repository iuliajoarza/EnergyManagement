# Distributed Systems Assignment 2: Asynchronous Communication

## Components

- **Device Data Simulator (Python):** Generates synthetic smart meter readings every 10 minutes and sends them to RabbitMQ as JSON.
- **Monitoring Microservice (Java Spring Boot):** Consumes device data from RabbitMQ, computes hourly energy totals, and stores results in PostgreSQL.
- **RabbitMQ:** Message broker for device data and synchronization events (Dockerized).
- **PostgreSQL:** Database for monitoring results (Dockerized).
- **Docker Compose:** Orchestrates all services.

## Build & Run Instructions

1. **Install Docker & Docker Compose**
   - Make sure Docker and Docker Compose are installed on your system.

2. **Build and Start All Services**
   ```bash
   sudo docker compose up --build
   ```

3. **Access the Application**
   - Frontend: http://localhost
   - Monitoring API: http://localhost/api/consumption
   - Traefik Dashboard: http://localhost:8080

4. **Device Data Simulator**
   - The simulator will start automatically and send data to RabbitMQ.
   - You can configure device ID and other parameters in the Python script.
   - Run `sync_producer.py` and `sync_device_producer.py` to send user/device sync events.

5. **Monitoring Microservice**
   - Consumes messages from RabbitMQ and stores hourly totals in PostgreSQL.
   - API endpoint `/api/consumption?deviceId=...&day=YYYY-MM-DD` returns hourly totals for charting.

6. **Database Access**
   - PostgreSQL runs on port 5432 (default user/password: `monitor/monitor`).

7. **Reverse Proxy**
   - Traefik routes requests to frontend and API services.

8. **Deployment Diagram**
   - See `uml_deployment_diagram.md` for architecture.

## Project Structure

- `simulation/simulator/` — Device Data Simulator (Python)
- `simulation/simulator/sync_producer.py` — User sync event producer
- `simulation/simulator/sync_device_producer.py` — Device sync event producer
- `monitoring/` — Monitoring Microservice (Java Spring Boot, top-level folder)
- `monitoring/src/main/java/com/example/monitoring/controller/ConsumptionController.java` — API for hourly consumption
- `simulation/frontend/energy-chart.js` — React chart component
- `simulation/docker-compose.yml` — Docker Compose file
- `simulation/traefik.yml` — Traefik reverse proxy config
- `simulation/uml_deployment_diagram.md` — Deployment diagram
- `simulation/README.md` — This file

## Example Data Message
```json
{
  "timestamp": "2025-11-30T10:00:00Z",
  "device_id": "device_1",
  "measurement_value": 1.23
}
```

## Notes
- All services are containerized for easy deployment.
- For inspiration, see: https://github.com/ArianaMarcu/Microservices-EnergyManagementSystem-Docker2
