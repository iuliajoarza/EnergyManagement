# Demo — Spring Boot API with React Frontend

A microservices project with Spring Boot REST APIs (people and device services) and a React frontend. Includes PostgreSQL databases, authentication, and Traefik routing.

## Contents

- **Backend Services**: People Service, Device Service, Auth Service
- **Frontend**: React SPA with authentication and CRUD operations
- **Database**: PostgreSQL for each service
- **Proxy**: Traefik for routing and load balancing

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