# UML Deployment Diagram

```mermaid
graph TD
  Simulator["Device Data Simulator (Python)"]
  RabbitMQ["RabbitMQ Broker"]
  Monitoring["Monitoring Microservice (Java)"]
  PostgreSQL["PostgreSQL Database"]
  Frontend["Web Frontend (React)"]
  Traefik["Traefik Reverse Proxy"]

  Simulator -->|JSON Device Data| RabbitMQ
  Monitoring -->|Consumes Data| RabbitMQ
  Monitoring -->|Stores Results| PostgreSQL
  Frontend -->|REST API| Monitoring
  Traefik -->|Routes Requests| Frontend
  Traefik -->|Routes Requests| Monitoring
```

- All services are containerized and orchestrated via Docker Compose.
- Traefik reverse proxy exposes frontend and API endpoints.
- RabbitMQ handles both device data and synchronization events.
