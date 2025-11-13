# Swagger UI Documentation

## ✅ Swagger/OpenAPI Added to All Services!

### 📚 API Documentation URLs:

Once all services are running, access Swagger UI at:

#### 1. **Authentication Service**
- **Swagger UI**: http://localhost/auth/swagger-ui.html
- **OpenAPI JSON**: http://localhost/auth/v3/api-docs
- **Port**: 8082 (internal), accessed via Traefik at `/auth`

#### 2. **People Management Service** 
- **Swagger UI**: http://localhost/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost/api/v3/api-docs
- **Port**: 8080 (internal), accessed via Traefik at `/api`

#### 3. **Device Management Service**
- **Swagger UI**: http://localhost/api/device/swagger-ui.html  
- **OpenAPI JSON**: http://localhost/api/device/v3/api-docs
- **Port**: 8081 (internal), accessed via Traefik at `/api/device`

---

## 🔐 How to Use Swagger with JWT Authentication:

1. **Get JWT Token**: 
   - Go to Authentication Service Swagger UI
   - Use `/auth/login` endpoint with credentials
   - Copy the JWT token from response

2. **Authorize**:
   - Click the **"Authorize" 🔓** button at top right
   - Enter: `Bearer <your-jwt-token>`
   - Click "Authorize"

3. **Test APIs**:
   - All authenticated endpoints will now work!
   - Green padlock 🔒 means endpoint requires authentication

---

## 🛠️ What Was Added:

### Dependencies (pom.xml):
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Configuration Files:
- `AuthService/src/main/java/com/AuthService/AuthService/config/OpenApiConfig.java`
- `demo/src/main/java/com/example/demo/config/OpenApiConfig.java`
- `microserviceDevice/src/main/java/org/example/microservicedevice/config/OpenApiConfig.java`

### Security Config Updates:
All `SecurityConfig.java` files updated to permit Swagger endpoints:
```java
.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
```

---

## 🚀 Next Steps:

1. **Rebuild all services** (already in progress):
   ```bash
   cd "/home/iulia/Desktop/ds2025_spring_example (2)"
   sudo docker compose build
   ```

2. **Restart containers**:
   ```bash
   sudo docker compose up -d
   ```

3. **Access Swagger UI** at the URLs above!

---

## 📝 API Features Documented:

### Authentication Service:
- ✅ POST `/auth/register` - Register new user
- ✅ POST `/auth/login` - Login and get JWT token
- ✅ POST `/auth/validate` - Validate JWT token

### People Service:
- ✅ GET `/user` - List all people (Admin) or self (User)
- ✅ GET `/user/{id}` - Get person by ID
- ✅ GET `/user?username={username}` - Get person by username
- ✅ POST `/user` - Create new person
- ✅ PUT `/user/{id}` - Update person
- ✅ DELETE `/user/{id}` - Delete person

### Device Service:
- ✅ GET `/device` - List devices (filtered by role)
- ✅ GET `/device/{id}` - Get device by ID
- ✅ GET `/device?userId={uuid}` - Get devices by user (Admin only)
- ✅ POST `/device` - Create device (Admin only)
- ✅ PUT `/device/{id}` - Update device
- ✅ DELETE `/device/{id}` - Delete/detach device
- ✅ DELETE `/device/user/{userId}` - Delete all user devices (Admin)
- ✅ PATCH `/device/user/{userId}/detach` - Detach devices from user (Admin)

---

## 🎨 Swagger UI Features:

- ✅ **Interactive API testing** - Try APIs directly in browser
- ✅ **Request/Response examples** - See sample payloads
- ✅ **Schema documentation** - View DTOs and validation rules
- ✅ **JWT Bearer authentication** - Built-in authorization
- ✅ **Role-based access** - Shows which endpoints require which roles

---

Enjoy your fully documented API! 🎉
