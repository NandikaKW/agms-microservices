# Automated Greenhouse Management System (AGMS)
## Microservice-Based Application — ITS 2018 Final Examination

---

## Architecture Overview

```
                        ┌──────────────────────┐
                        │   Config Server       │
                        │   Port: 8888          │
                        │   (Native Profile)    │
                        └──────────┬───────────┘
                                   │ serves config
        ┌──────────────────────────┼──────────────────────────┐
        │                          │                          │
┌───────▼───────┐   ┌──────────────▼──────────────┐   ┌───────▼───────┐
│ Eureka Server │   │      API Gateway            │   │ External IoT  │
│ Port: 8761    │◄──│      Port: 8080             │   │ API (Live)    │
│ (Registry)    │   │      (JWT Security)         │   │ 104.211.95.241│
└───────▲───────┘   └──────────────┬──────────────┘   └───────▲───────┘
        │ registers                │ routes                    │
        │                          │                           │
   ┌────┼──────────┬───────────────┼───────────────┐           │
   │    │          │               │               │           │
┌──▼────┴──┐ ┌────▼─────┐ ┌──────▼──────┐ ┌──────▼──────┐    │
│Zone Svc  │ │Sensor Svc│ │Automation   │ │Crop Service │    │
│Port:8081 │ │Port:8082 │ │Service 8083 │ │Port: 8084   │    │
│          │ │          │ │             │ │             │    │
│ CRUD     │ │ Scheduled│ │ Rule Engine │ │ State       │    │
│ Zones    │ │ Fetcher  │ │ TURN_FAN_ON │ │ Machine     │    │
│ minTemp  │ │ every 10s│ │ TURN_HEATER │ │ SEEDLING→   │    │
│ maxTemp  │ │          │ │             │ │ VEGETATIVE→ │    │
│          │ │ Pushes──►│ │◄──Feign──►  │ │ HARVESTED   │    │
│ Registers│ │ to Auto  │ │ to Zone Svc │ │             │    │
│ Device───┼─┤ Service  │ │             │ │             │    │
│ via IoT  │ │ Fetches──┼─┼─────────────┼─┼─────────────┼────┘
│ API      │ │ from IoT │ │             │ │             │
└──────────┘ └──────────┘ └─────────────┘ └─────────────┘
```

## Technology Stack

| Technology | Purpose |
|-----------|---------|
| Spring Boot 3.5.3 | Core framework |
| Spring Cloud 2025.0.0 | Microservices infrastructure |
| Spring Cloud Eureka | Service discovery |
| Spring Cloud Config | Centralized configuration |
| Spring Cloud Gateway | API routing + JWT security |
| OpenFeign | Inter-service communication (Automation → Zone) |
| RestTemplate (@LoadBalanced) | Inter-service communication (Sensor → Automation) |
| MySQL | Database (AGMS_Db) |
| JWT (JJWT) | API Gateway security |
| External IoT API | Live telemetry data |

## Prerequisites

- **Java 21+** (JDK)
- **Maven 3.9+**
- **MySQL** (via XAMPP or standalone) — running on port 3306
- **Postman** — for API testing

## Database Setup

1. Start MySQL (via XAMPP Control Panel → Start MySQL)
2. The database `AGMS_Db` will be created automatically on first startup (`createDatabaseIfNotExist=true`)
3. Default credentials: `root` / (empty password)

## Startup Order (CRITICAL)

You **MUST** start services in this exact order. Each service needs the previous ones running.

### Step 1: Config Server
```bash
cd ConfigServer
mvn clean spring-boot:run
```
Wait for: `Started CloudConfigApplication in X seconds`

### Step 2: Eureka Server
```bash
cd Eureka_Sever
mvn clean spring-boot:run
```
Wait for: `Started EurekaServerApplication in X seconds`

### Step 3: API Gateway
```bash
cd Api_Gatway
mvn clean spring-boot:run
```
Wait for: `Started ApiGatewayApplication in X seconds`

### Step 4: Domain Services (can start in any order)
```bash
# Terminal 4
cd ZoneService
mvn clean spring-boot:run

# Terminal 5
cd SensorService
mvn clean spring-boot:run

# Terminal 6
cd AutomationService
mvn clean spring-boot:run

# Terminal 7
cd CropService
mvn clean spring-boot:run
```

## Verify Eureka Dashboard

Open [http://localhost:8761](http://localhost:8761) in your browser. You should see:

| Application | Port | Status |
|------------|------|--------|
| CONFIG-SERVER | 8888 | UP |
| API-GATEWAY | 8080 | UP |
| ZONE-SERVICE | 8081 | UP |
| SENSOR-SERVICE | 8082 | UP |
| AUTOMATION-SERVICE | 8083 | UP |
| CROP-SERVICE | 8084 | UP |

## API Endpoints (via Gateway - Port 8080)

### Zone Management Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/zones` | Create zone + register IoT device |
| GET | `/api/zones/{id}` | Get zone by ID |
| PUT | `/api/zones/{id}` | Update zone thresholds |
| DELETE | `/api/zones/{id}` | Delete zone |

**Example POST /api/zones:**
```json
{
    "name": "Tomato Zone",
    "minTemp": 18.0,
    "maxTemp": 32.0
}
```

### Sensor Telemetry Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/sensors/latest` | Get last fetched reading |

*Note: Sensor Service automatically fetches data every 10 seconds from the External IoT API and pushes to Automation Service.*

### Automation & Control Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/automation/process` | Receive sensor data (internal) |
| GET | `/api/automation/logs` | View triggered actions |

### Crop Inventory Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/crops` | Register new crop batch |
| PUT | `/api/crops/{id}/status?status=VEGETATIVE` | Update lifecycle |
| GET | `/api/crops` | View all crops |

**Example POST /api/crops:**
```json
{
    "cropName": "Tomato",
    "zoneId": "Zone-Tomato",
    "quantity": 200
}
```

## External IoT API Integration

- **Base URL**: `http://104.211.95.241:8080/api`
- **Auth**: JWT-based (Register → Login → Bearer Token)
- **Zone Service**: Registers devices on zone creation
- **Sensor Service**: Fetches live telemetry every 10 seconds

## Security (JWT at API Gateway)

The API Gateway implements JWT validation via `JwtAuthenticationFilter`:
- All requests must include `Authorization: Bearer <token>` header
- Requests without valid tokens receive `401 Unauthorized`
- Public endpoints: `/auth/login`, `/auth/register`

## Postman Collection

Import `postman/agms-postman-collection.json` into Postman for ready-to-use API tests.

## Project Structure

```
AGMS/
├── ConfigServer/          # Port 8888 - Centralized Configuration
├── Eureka_Sever/          # Port 8761 - Service Registry
├── Api_Gatway/            # Port 8080 - API Gateway + JWT Security
├── ZoneService/           # Port 8081 - Zone Management
├── SensorService/         # Port 8082 - Sensor Telemetry Bridge
├── AutomationService/     # Port 8083 - Rule Engine
├── CropService/           # Port 8084 - Crop Lifecycle
├── postman/               # Postman API Collection
├── docs/                  # Screenshots & Documentation
└── README.md              # This file
```