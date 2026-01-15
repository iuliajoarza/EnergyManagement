# Energy Management System - Microservices Architecture

A distributed microservices project for managing energy consumption with real-time notifications, chatbot support, and WebSocket communication. Built with Spring Boot, React, RabbitMQ, and Docker.

## 🎯 Assignment 3 Features

### ✅ Implemented Features (5 points minimum)
- **WebSocket Microservice**: Real-time overconsumption notifications via WebSocket/STOMP
- **Rule-Based Chatbot**: Customer support with 14+ predefined rules for common questions
- **Overconsumption Alerts**: Automatic detection and real-time notifications when devices exceed max consumption
- **Chat UI**: Floating chat widget integrated in frontend with live messaging
- **Docker Deployment**: All services containerized and orchestrated with Docker Compose

### 🎁 Bonus Features
- **AI-Driven Customer Support (1p)** - ✅ IMPLEMENTED with HuggingFace API (Mistral-7B model)
  - Falls back to AI when no rule matches
  - See [HUGGINGFACE_SETUP.md](HUGGINGFACE_SETUP.md) for configuration
- Client-Admin Chat (2p) - Architecture ready, needs admin panel
- Load Balancing Service (2p) - Can be added for monitoring replicas

## 📦 Contents

- **Backend Services**:
  - People Service (User management)
  - Device Service (Device CRUD operations)
  - Auth Service (JWT authentication)
  - Monitoring Service (Energy consumption tracking + alerts)
  - **WebSocket Service** (Real-time notifications - NEW)
  - **Customer Support Service** (Rule-based chatbot - NEW)
  
- **Frontend**: React SPA with WebSocket client, chat interface, and real-time notifications
- **Message Broker**: RabbitMQ for asynchronous communication
- **Databases**: PostgreSQL instances for each service
- **Reverse Proxy**: Traefik for routing and load balancing
- **Simulator**: Python-based device data generator

## Project structure
```
demo/
├── .mvn
│   └── wrapper
│       └── maven-wrapper.properties
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── demo
│   │   │               ├── controllers
│   │   │               │   └── PersonController.java
│   │   │               ├── dtos
│   │   │               │   ├── builders
│   │   │               │   │   └── PersonBuilder.java
│   │   │               │   ├── validators
│   │   │               │   │   ├── annotation
│   │   │               │   │   │   └── AgeLimit.java
│   │   │               │   │   └── AgeValidator.java
│   │   │               │   ├── PersonDetailsDTO.java
│   │   │               │   └── PersonDTO.java
│   │   │               ├── entities
│   │   │               │   └── Person.java
│   │   │               ├── handlers
│   │   │               │   ├── exceptions
│   │   │               │   │   └── model
│   │   │               │   │       ├── CustomException.java
│   │   │               │   │       ├── ExceptionHandlerResponseDTO.java
│   │   │               │   │       └── ResourceNotFoundException.java
│   │   │               │   └── RestExceptionHandler.java
│   │   │               ├── repositories
│   │   │               │   └── PersonRepository.java
│   │   │               ├── services
│   │   │               │   └── PersonService.java
│   │   │               └── DemoApplication.java
│   │   └── resources
│   │       ├── static
│   │       ├── templates
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── example
│                   └── demo
│                       └── DemoApplicationTests.java
├── .gitattributes
├── .gitignore
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── postman_collection.json
```

- `src/main/...` — SpringBoot source
- `src/main/resources/application.properties` — app configuration
- `postman_collection.json` — Postman collection to import
- `pom.xml` — Maven project wht Spring Boot 4.0.0-SNAPSHOT and Java 25

## Prerequisites
- **Java JDK 25**
- **PostgreSQL** server accessible from the app (can be changed to any other db from application.properties)
- **Postman** account to import & run the test collection

## 🚀 Quick Start (Assignment 3)

### Prerequisites
- **Docker** and **Docker Compose**
- **Node.js 16+** (for frontend development)
- **Java 17+** (for local development)
- **PostgreSQL** (automatically handled by Docker)

### Build and Run All Services
```bash
# Set JWT secret (important!)
export JWT_SECRET=iuliaiuliaiuliaiuliaiuliaiulia01112003

# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

### Access the Application
- **Frontend**: http://localhost
- **Traefik Dashboard**: http://localhost:8080
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **People Service**: http://localhost:8081
- **Device Service**: http://localhost:8082
- **Auth Service**: http://localhost:8083
- **Monitoring Service**: http://localhost:8090
- **WebSocket Service**: http://localhost:8085
- **Chat Service**: http://localhost:8086

### Test the Features

#### 1. Login and Navigate
```bash
# Default credentials (if seeded)
Username: admin
Password: admin
```

#### 2. Add a Device with Max Consumption
- Go to "Devices" section
- Click "Add New Device"
- Set **Max Consumption** (e.g., 2.5 kW)
- Save the device

#### 3. Start the Simulator
```bash
cd simulation
./run_simulator.sh
```
The simulator will generate energy data every 5 seconds.

#### 4. See Overconsumption Alerts
- When device consumption exceeds max, you'll receive:
  - **Real-time notification** in the chat widget
  - **Browser notification** (if permitted)
  - **System message** in chat history

#### 5. Test the Chatbot
Click the chat icon (💬) in the bottom-right corner and try these messages:
- "hello" - Greeting
- "how to add device" - Device management help
- "alert" - Information about overconsumption alerts
- "factura" - Billing information
- "admin" - Contact administrator
- Any other question - Default response

## 📊 Architecture Overview

### Microservices Communication
```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Simulator  │────▶│   RabbitMQ   │────▶│ Monitoring  │
│   (Python)  │     │              │     │  Service    │
└─────────────┘     └──────┬───────┘     └──────┬──────┘
                           │                     │
                           │                     │ (Overconsumption)
                           │                     ▼
                    ┌──────▼───────┐     ┌─────────────┐
                    │   WebSocket  │◀────│  Alert      │
                    │   Service    │     │  Publisher  │
                    └──────┬───────┘     └─────────────┘
                           │
                           │ (WebSocket/STOMP)
                           ▼
                    ┌─────────────┐
                    │   Frontend  │
                    │   (React)   │
                    └─────────────┘
```

### Chat Flow
```
User Message ──▶ WebSocket ──▶ RabbitMQ ──▶ Chat Service ──▶ Rule Engine
                                                    │
                                                    ▼
                                            ┌───────────────┐
                                            │ 14+ Rules     │
                                            │ - Greeting    │
                                            │ - Device Help │
                                            │ - Alerts      │
                                            │ - Billing     │
                                            │ - etc.        │
                                            └───────┬───────┘
                                                    │
Bot Response ◀── WebSocket ◀── RabbitMQ ◀──────────┘
```

## Database (PostgreSQL) — ( !!! Create it first !!!)
The app expects a PostgreSQL database to already exist. Default connection values:
```
DB_IP=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=root
DB_DBNAME=example-db
```

> Note: Hibernate is set to `spring.jpa.hibernate.ddl-auto=update`, so tables will be created/updated automatically on first run

## Configuration
All important settings are in `src/main/resources/application.properties`. You can override them via environment variables:

| Purpose | Property | Env var | Default |
|---|---|---|---|
| DB host | `database.ip` | `DB_IP` | `localhost` |
| DB port | `database.port` | `DB_PORT` | `5432` |
| DB user | `database.user` | `DB_USER` | `postgres` |
| DB password | `database.password` | `DB_PASSWORD` | `root` |
| DB name | `database.name` | `DB_DBNAME` | `example-db` |
| HTTP port | `server.port` | `PORT` | `8080` |

Effective JDBC URL:
```
jdbc:postgresql://${DB_IP}:${DB_PORT}/${DB_DBNAME}
```

## How to run (local)
From the project root (`demo/`), run with the Maven Wrapper:

```bash
# 1) export env vars if you need non-defaults
export DB_IP=localhost
export DB_PORT=5432
export DB_USER=postgres
export DB_PASSWORD=root
export DB_DBNAME=example-db
export PORT=8080

# 2) start the app
./mvnw spring-boot:run
```

The app will start on: **http://localhost:8080** (unless you changed `PORT`).

## API quick peek
The included Postman collection targets the **people** resource defined by the **Person** entity.
Examples once the app is running:
- `GET /people` — list all
- `POST /people` — create (body: JSON person)
- `GET /people/{personId}` — fetch one
- `PUT /people/{personId}` — update
- `DELETE /people/{personId}` — delete

## Test with Postman
1. Create/sign in to your **Postman** account;
2. **Import** the collection file: [`postman_collection.json`];
3. In Postman, verify the collection variables so that you know everything is set up correctly:
   - `baseUrl` → `http://localhost:8080`
   - `resource` → `people`
4. Run the requests in order (the collection includes a test that remembers `personId` after create) 

## Frontend Setup

The React frontend provides a user-friendly interface for managing people and devices.

### Prerequisites
- **Node.js 16+** and **npm**

### Installation and Running
```bash
cd frontend
npm install
npm start
```

The frontend will start on: **http://localhost:3000**

### Features
- **Authentication**: Login with JWT tokens
- **People Management**: Create, read, update, delete people
- **Device Management**: Create, read, update, delete devices
- **User Assignment**: Assign devices to users
- **Responsive Design**: Works on desktop and mobile

### Frontend Structure
```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── Layout.js          # Main layout with navigation
│   │   ├── Login.js           # Authentication form
│   │   ├── People.js          # People management
│   │   ├── Devices.js         # Device management
│   │   └── ProtectedRoute.js  # Auth guard
│   ├── context/
│   │   └── AuthContext.js     # Authentication state
│   ├── services/
│   │   └── api.js             # API communication
│   ├── App.js                 # Main app component
│   └── index.js               # Entry point
└── package.json
```

### Default Credentials
For testing, you can use the basic auth credentials configured in Traefik:
- Username: `user`
- Password: `test`

## Where it runs
By default the app binds to `PORT` (default **8080**) on your machine

---

## Run with Docker + Traefik (URLs)
If you started the stack with:

- docker compose up -d

Then open these in your browser (address bar), a.k.a. "what URL to write":

- Through Traefik (with Basic Auth):
  - People API: http://localhost/api/people
  - Devices API: http://localhost/api/devices
  - Credentials (Basic Auth): username "user", password "test"
- Traefik dashboard: http://localhost:8080
- Direct access to services (bypassing Traefik):
  - People service: http://localhost:8081/people
  - Devices service: http://localhost:8082/devices

Notes:
- Traefik strips the "/api" prefix before forwarding to the services, so /api/people reaches /people on the people service, and /api/devices reaches /devices on the devices service.
- If http://localhost/api/people asks for a username/password, use user/test.

## Full Stack Development Workflow

1. **Start the backend services**:
   ```bash
   docker compose up -d
   ```

2. **Start the frontend**:
   ```bash
   cd frontend
   npm install
   npm start
   ```

3. **Access the application**:
   - Frontend: http://localhost:3000
   - Backend APIs: http://localhost/api/people, http://localhost/api/devices
   - Traefik Dashboard: http://localhost:8080

## Full Stack Docker Deployment

The entire application stack can be run with Docker Compose, including the React frontend.

### Prerequisites
- **Docker** and **Docker Compose**

### Quick Start
```bash
# Build and start all services
docker compose up -d

# Check service status
docker compose ps

# View logs
docker compose logs -f frontend
```

### Services Overview
When running with Docker Compose:

| Service | Container | Port | Description |
|---------|-----------|------|-------------|
| Frontend | `frontend` | 80 | React SPA served by Nginx |
| Traefik | `traefik` | 80, 8080 | Reverse proxy and load balancer |
| People API | `spring-demo1` | 8080 | People management service |
| Device API | `spring-demo2` | 8080 | Device management service |
| Auth API | `auth-service` | 8080 | Authentication service |
| PostgreSQL | `postgres-demo1` | 5432 | People database |
| PostgreSQL | `postgres-demo2` | 5432 | Device database |
| PostgreSQL | `postgres-auth` | 5432 | Auth database |

### Access URLs
- **Frontend Application**: http://localhost
- **Traefik Dashboard**: http://localhost:8080
- **API Endpoints**:
  - People: http://localhost/api/people
  - Devices: http://localhost/api/devices  
  - Auth: http://localhost/api/auth

### Development Workflow

#### Option 1: Full Docker Stack
```bash
# Start everything
docker compose up -d

# Rebuild frontend after changes
docker compose build frontend
docker compose up -d frontend

# View logs
docker compose logs -f frontend
```

#### Option 2: Frontend Development Mode
```bash
# Start backend services only
docker compose up -d postgres-demo1 postgres-demo2 postgres-auth spring-demo1 spring-demo2 auth-service traefik

# Run frontend in development mode
cd frontend
npm install
npm start
# Frontend will be available at http://localhost:3000
```

### Frontend Docker Details

The frontend uses a multi-stage build:
1. **Build stage**: Node.js to build the React app
2. **Production stage**: Nginx to serve static files

**Key features**:
- Optimized production build
- Nginx configuration for SPA routing
- Environment variable support
- Security headers
- Static asset caching
- Gzip compression

### Troubleshooting

#### Frontend not loading
```bash
# Check frontend container
docker compose logs frontend

# Rebuild frontend
docker compose build --no-cache frontend
docker compose up -d frontend
```

#### API calls failing
```bash
# Check backend services
docker compose logs spring-demo1
docker compose logs spring-demo2
docker compose logs auth-service

# Check Traefik routing
docker compose logs traefik
```

#### Database connection issues
```bash
# Check database containers
docker compose logs postgres-demo1
docker compose logs postgres-demo2
docker compose logs postgres-auth

# Reset databases
docker compose down -v
docker compose up -d
```

### Environment Variables

Frontend environment variables can be set in `/frontend/.env`:
```env
REACT_APP_API_URL=http://localhost
GENERATE_SOURCEMAP=false
```


┌─────────────────────────────────────────────────────────────────────────────┐
│                        RabbitMQ Communication Schema                        │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 1. USER & DEVICE SYNC EVENTS (Broadcast - Fanout Exchange)                  │
└──────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │ demo Service │ (SyncPublisherService)
    └──────┬───────┘
           │ publishes: user, user_deleted events
           ↓
    ┌──────────────────────────┐
    │ sync.events.exchange     │ (FANOUT - broadcast to all)
    │ (durable)                │
    └──────┬───────────────────┘
           │
           ├──────────────────────────────────────────────────┐
           │                                                  │
           ↓                                                  ↓
    ┌─────────────────┐                            ┌──────────────────┐
    │ sync.events.device queue                     │ Simulator        │
    └────────┬────────┘                            └────────┬─────────┘
             │                                              │
             ↓                                              ↓
    ┌─────────────────────┐                       • Caches max_consumption
    │ microserviceDevice  │                       • Detects device_deleted
    │ (UserCacheService)  │                       • Auto-stops simulation
    └─────────────────────┘
             │
             ↓
    • Caches user data locally
    • Handles user_deleted events


    ┌────────────────────┐
    │ microserviceDevice │ (SyncPublisherService)
    └──────────┬─────────┘
               │ publishes: device, device_deleted events
               │            (includes max_consumption)
               ↓
    ┌──────────────────────────┐
    │ sync.events.exchange     │ (FANOUT - reuses same exchange)
    │ (durable)                │
    └──────┬───────────────────┘
           │
           ├──────────────────────────────────────────────────┐
           │                                                  │
           ↓                                                  ↓
    ┌─────────────────┐                            ┌──────────────────┐
    │ sync.events.device queue                     │ Simulator        │
    └────────┬────────┘                            └────────┬─────────┘
             │                                              │
             ↓                                              ↓
    ┌─────────────────────┐                       • Receives device events
    │ microserviceDevice  │                       • Caches max_consumption
    │ (UserCacheService)  │                       • Clamps measurements
    └─────────────────────┘

## 🐛 Troubleshooting

### WebSocket Connection Issues
```bash
# Check if websocket service is running
docker-compose ps websocket-service

# Check logs
docker-compose logs -f websocket-service

# Verify RabbitMQ connection
docker-compose exec websocket-service curl localhost:8085/actuator/health
```

### Chat Not Responding
```bash
# Check chat service logs
docker-compose logs -f chat-service

# Verify RabbitMQ queues exist
# Go to http://localhost:15672 and check:
# - chat.user.messages
# - chat.bot.responses
```

### No Overconsumption Alerts
1. Verify device has `max_consumption` set
2. Check monitoring service is processing data:
   ```bash
   docker-compose logs -f monitoring
   ```
3. Ensure WebSocket is connected (check browser console)
4. Verify overconsumption queue exists in RabbitMQ

### Frontend Not Loading
```bash
# Rebuild frontend
docker-compose build frontend
docker-compose up -d frontend

# Check logs
docker-compose logs frontend
```

### Database Connection Errors
```bash
# Check all databases are running
docker-compose ps | grep db

# Restart databases
docker-compose restart db1 db2 db-auth monitoring-db
```

## 📝 Assignment 3 Checklist

### Minimum Requirements (5 points) ✅
- [x] WebSocket Microservice for overconsumption notifications
- [x] Integration with Assignment 2 (monitoring)
- [x] Rule-based chatbot with 10+ rules (implemented 14 rules)
- [x] README.md with build and execution instructions
- [x] Docker deployment with docker-compose

### Bonus Features (Optional)
- [ ] AI-driven customer support (+1p) - Architecture ready
- [ ] Client-Admin chat integration (+2p) - Architecture ready
- [ ] Load balancing service (+2p) - Can be added

### Project Requirements (10 points)
- [x] Traefik reverse proxy configured
- [x] Docker deployment with multiple services
- [ ] UML Deployment diagram (needs to be created)

## 📚 Technologies Used

- **Backend**: Spring Boot 3.2.0, Java 17
- **Frontend**: React 18, WebSocket (SockJS + STOMP)
- **Message Broker**: RabbitMQ 3
- **Databases**: PostgreSQL 16
- **Reverse Proxy**: Traefik 2.11
- **Containerization**: Docker, Docker Compose
- **Real-time**: WebSocket, STOMP protocol
- **AI Ready**: Gemini/OpenAI integration prepared

## 👥 Contact

For questions or issues regarding Assignment 3:
- Check RabbitMQ Management UI for message flow
- Review docker-compose logs for each service
- Verify WebSocket connections in browser console
- Test rules by sending messages in chat

---

**Assignment 3 - Distributed Systems 2025-2026**  
Faculty of Automation and Computer Science, UTCN
             │
             ↓
    • Updates user_cache with device info


┌──────────────────────────────────────────────────────────────────────────────┐
│ 2. USER COMMANDS (Targeted - Direct Queue)                                  │
└──────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │ demo Service │ (SyncPublisherService)
    └──────┬───────┘
           │ publishes: delete_user_devices command
           ↓
    ┌──────────────────────────┐
    │ user.commands queue      │ (durable, direct)
    │                          │
    └──────┬───────────────────┘
           │
           ↓
    ┌──────────────────────────────┐
    │ microserviceDevice           │ (UserCommandsConsumerService)
    └──────────────────────────────┘
           │
           ↓
    • Receives delete_user_devices command
    • Calls detachDevicesFromUser(userId)
    • Sets userId = NULL for all user's devices
    • Publishes device sync events


┌──────────────────────────────────────────────────────────────────────────────┐
│ 3. AUTH COMMANDS (Targeted - Direct Queue)                                  │
└──────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │ demo Service │ (AuthSyncService)
    └──────┬───────┘
           │ publishes: create_auth_user, update_auth_user, delete_auth_user
           ↓
    ┌──────────────────────────┐
    │ auth.commands queue      │ (durable, direct)
    │                          │
    └──────┬───────────────────┘
           │
           ↓
    ┌──────────────────────────┐
    │ AuthService              │ (AuthCommandsConsumer)
    └──────────────────────────┘
           │
           ↓
    • Receives auth commands
    • Creates/updates/deletes users in authdb
    • Encodes passwords with BCrypt
    • Assigns roles (ROLE_USER, ROLE_ADMIN)


┌──────────────────────────────────────────────────────────────────────────────┐
│ 4. ENERGY DATA STREAM (Direct Queue - High Volume)                          │
└──────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │ Simulator    │ (Python script)
    └──────┬───────┘
           │ publishes: {timestamp, device_id, measurement_value}
           │ Rate: ~0.20/sec per device (every 5 seconds)
           ↓
    ┌──────────────────────────┐
    │ energy_data queue        │ (non-durable, direct)
    │                          │
    └──────┬───────────────────┘
           │
           ↓
    ┌──────────────────────────────┐
    │ monitoring Service           │ (DeviceDataConsumerService)
    └──────────────────────────────┘
           │
           ↓
    • Receives raw measurements
    • Stores in device_data table
    • Aggregates into hourly_energy_consumption
    • Groups by device_id and hour


┌──────────────────────────────────────────────────────────────────────────────┐
│ SUMMARY                                                                      │
└──────────────────────────────────────────────────────────────────────────────┘

Exchanges:
  • sync.events.exchange (fanout, durable) - broadcasts all sync events

Queues:
  • sync.events.device (durable) - device service listens for user/device events
  • user.commands (durable) - device service listens for user commands
  • auth.commands (durable) - auth service listens for auth commands
  • energy_data (non-durable) - monitoring service listens for measurements

Producers:
  • demo Service → sync events, user commands, auth commands
  • microserviceDevice → device sync events
  • Simulator → energy measurements

Consumers:
  • microserviceDevice → sync events, user commands
  • AuthService → auth commands
  • monitoring Service → energy data
  • Simulator → device sync events (for max_consumption & auto-stop)

Pattern: Event-driven microservices - zero HTTP between services, all via RabbitMQ