Is this a microservice?

Short answer: Yes — the Spring Boot application under src/main/java is implemented as a small, independently runnable REST service, which fits a typical microservice profile. The device service now has its own Spring Boot entry point and can run independently as well.

Why it qualifies as a microservice
- Independent process: It’s a Spring Boot app (DemoApplication) that runs on its own embedded server (default port 8080) and can be deployed independently.
- Single, focused domain: It exposes a narrow REST API for managing Person resources (PersonController, PersonService, PersonRepository).
- Own data boundary: It uses its own database connection (PostgreSQL via spring-boot-starter-data-jpa) configured in src/main/resources/application.properties.
- Clear API boundary: All external interaction happens through HTTP endpoints under /people.

What it is not
- It is not a full microservice system by itself. A microservice architecture normally involves many independently deployable services, service discovery, observability, gateways, etc. This repository shows two simple services (people-service and device-service), not the entire ecosystem.
- This repository is not a full multi-module build; both services live in the same Maven project for simplicity.

How to run
1) Requirements
   - Java 25 (see pom.xml java.version)
   - Maven 3.9+
   - A reachable PostgreSQL instance (or override env vars to point to one)
2) Configure database (optional; defaults provided via env vars in application.properties):
   - DB_IP (default: localhost)
   - DB_PORT (default: 5432)
   - DB_USER (default: iulia)
   - DB_PASSWORD (default: iulia)
   - DB_DBNAME (default: testdb)
3) Build and run the Spring Boot app from the project root:
   - mvn -q -DskipTests spring-boot:run
   - Or build a jar: mvn -q -DskipTests package and run it with java -jar target/demo-0.0.1-SNAPSHOT.jar

Available endpoints (examples)
- GET  /people
- POST /people        (body: PersonDetailsDTO)
- GET  /people/{id}
- DELETE /people/{id}

Notes
- The root pom.xml uses packaging pom and declares a module device. While unusual (source code exists alongside a parent pom), the Spring Boot app still builds and runs. If you prefer a conventional layout, consider:
  - Converting this repository into a proper multi-module build: move the Spring Boot app into its own module (e.g., people-service/) with packaging jar and keep the root as a parent pom only.
  - Or remove modules and set the root project’s packaging to jar if you only want a single service here.

Enhancements often used in microservices (optional)
- Add Spring Boot Actuator for health/info readiness/liveness endpoints.
- Containerize (Dockerfile) and define resource limits.
- Add centralized configuration, service discovery, tracing, and metrics if part of a larger system.

Is this a microservice?

Short answer: Yes — the Spring Boot application under src/main/java is implemented as a small, independently runnable REST service, which fits a typical microservice profile. The device service now has its own Spring Boot entry point and can run independently as well.

Why it qualifies as a microservice
- Independent process: It’s a Spring Boot app (DemoApplication) that runs on its own embedded server (default port 8080) and can be deployed independently.
- Single, focused domain: It exposes a narrow REST API for managing Person resources (PersonController, PersonService, PersonRepository).
- Own data boundary: It uses its own database connection (PostgreSQL via spring-boot-starter-data-jpa) configured in src/main/resources/application.properties.
- Clear API boundary: All external interaction happens through HTTP endpoints under /people.

What it is not
- It is not a full microservice system by itself. A microservice architecture normally involves many independently deployable services, service discovery, observability, gateways, etc. This repository shows two simple services (people-service and device-service), not the entire ecosystem.
- This repository is not a full multi-module build; both services live in the same Maven project for simplicity.

How to run
1) Requirements
   - Java 25 (see pom.xml java.version)
   - Maven 3.9+
   - A reachable PostgreSQL instance (or override env vars to point to one)
2) Configure databases (optional; defaults provided):
   People service (application.properties):
   - DB_IP (default: localhost)
   - DB_PORT (default: 5432)
   - DB_USER (default: iulia)
   - DB_PASSWORD (default: iulia)
   - DB_DBNAME (default: testdb)
   Device service (application-device.properties, activated automatically by DeviceApplication):
   - DEVICE_DB_IP (default: localhost)
   - DEVICE_DB_PORT (default: 5432)
   - DEVICE_DB_USER (default: iulia)
   - DEVICE_DB_PASSWORD (default: iulia)
   - DEVICE_DB_NAME (default: devicedb)
   - DEVICE_DB_SCHEMA (default: public)
   - DEVICE_DDL_AUTO (default: update) — set to none if your DB user lacks DDL rights, or create/create-drop for fresh schemas
   3) Build and run each service from the project root:
    People service (port 8080):
    - mvn -q -DskipTests spring-boot:run
    - Or: mvn -q -DskipTests package && java -jar target/demo-0.0.1-SNAPSHOT.jar
    Device service (port 8081 by default):
    - mvn -q -DskipTests spring-boot:run -Dspring-boot.run.main-class=com.example.device.DeviceApplication
    - Or: mvn -q -DskipTests package && java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.main.sources=com.example.device.DeviceApplication
    To override ports: set PORT for people, or DEVICE_SERVICE_PORT for device, or pass --server.port.

Available endpoints (examples)
- GET  /people
- POST /people        (body: PersonDetailsDTO)
- GET  /people/{id}
- DELETE /people/{id}
- GET  /devices
- POST /devices       (body: DeviceDetailsDTO)
- GET  /devices/{id}
- POST /devices/{id}/user/{userId}   (assign a user to a device)
- DELETE /devices/user/{userId}      (delete devices by userId)
- DELETE /devices/{id}

Notes
- The people service (DemoApplication) now limits component scanning to com.example.demo so it does not load device controllers/entities or touch the device database when run. The device service is only loaded when starting com.example.device.DeviceApplication.
- The root pom.xml uses packaging jar for a single Maven module that contains both services. If you prefer a conventional multi-module layout, you can split into people-service/ and device-service/ modules with a parent pom.

Enhancements often used in microservices (optional)
- Add Spring Boot Actuator for health/info readiness/liveness endpoints.
- Containerize (Dockerfile) and define resource limits.
- Add centralized configuration, service discovery, tracing, and metrics if part of a larger system.
