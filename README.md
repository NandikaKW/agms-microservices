# **Automated Greenhouse Management System (AGMS)**

**Microservice-Based Application — ITS 2018 Final Examination**

---

## **📌 Project Overview**

The **Automated Greenhouse Management System (AGMS)** is a cloud-native, microservice-based platform designed to automate greenhouse operations using real-time environmental data.

The system integrates with an **external IoT API** to fetch live telemetry (temperature & humidity), processes it using a rule engine, and triggers automated actions to maintain optimal crop conditions.

---

## **🎯 Key Features**

* Microservices architecture using Spring Boot & Spring Cloud
* Service discovery using Eureka
* Centralized configuration via Config Server
* API Gateway with JWT-based authentication
* Real-time IoT data integration
* Automated rule-based decision engine
* Crop lifecycle management

---

## **🏗️ System Architecture**

```
                        ┌──────────────────────┐
                        │   Config Server       │
                        │   Port: 8888          │
                        └──────────┬───────────┘
                                   │
        ┌──────────────────────────┼──────────────────────────┐
        │                          │                          │
┌───────▼───────┐   ┌──────────────▼──────────────┐   ┌───────▼───────┐
│ Eureka Server │   │      API Gateway            │   │ External IoT  │
│ Port: 8761    │◄──│      Port: 8080             │   │ API (Live)    │
└───────▲───────┘   └──────────────┬──────────────┘   └───────────────┘
        │                          │
   ┌────┼──────────┬───────────────┼───────────────┐
   │    │          │               │               │
┌──▼────┴──┐ ┌────▼─────┐ ┌──────▼──────┐ ┌──────▼──────┐
│Zone Svc  │ │Sensor Svc│ │Automation   │ │Crop Service │
│Port:8081 │ │Port:8082 │ │Port:8083    │ │Port:8084    │
└──────────┘ └──────────┘ └─────────────┘ └─────────────┘
```

---

## **⚙️ Technology Stack**

| Technology       | Purpose                        |
| ---------------- | ------------------------------ |
| Spring Boot      | Microservices development      |
| Spring Cloud     | Distributed system support     |
| Eureka           | Service discovery              |
| Config Server    | Centralized configuration      |
| API Gateway      | Routing + JWT security         |
| OpenFeign        | Inter-service communication    |
| RestTemplate     | Internal service communication |
| MySQL            | Database                       |
| JWT              | Authentication                 |
| External IoT API | Live telemetry                 |

---

## **📋 Prerequisites**

* Java 21+
* Maven 3.9+
* MySQL (XAMPP or standalone)
* Postman

---

## **🗄️ Database Setup**

1. Start MySQL using XAMPP
2. Default configuration:

   ```
   Username: root
   Password: (empty)
   ```
3. Database will be auto-created:

   ```
   AGMS_Db
   ```

---

## **🚀 Running the Application (IMPORTANT)**

### ✅ **Option 1: Run All Services (Recommended)**

```powershell
.\start.ps1
```

Wait **2–3 minutes**, then verify:

👉 [http://localhost:8761](http://localhost:8761)

---

### ✅ **Option 2: Run via IntelliJ IDEA (Manual)**

Run services in the **exact order below**:

1. **Config Server**
2. **Eureka Server**
3. **API Gateway**
4. **Zone Service**
5. **Sensor Service**
6. **Automation Service**
7. **Crop Service**

---

## **📊 Eureka Dashboard Verification**

Open:
👉 [http://localhost:8761](http://localhost:8761)

Expected status:

| Service            | Port | Status |
| ------------------ | ---- | ------ |
| CONFIG-SERVER      | 8888 | UP     |
| API-GATEWAY        | 8080 | UP     |
| ZONE-SERVICE       | 8081 | UP     |
| SENSOR-SERVICE     | 8082 | UP     |
| AUTOMATION-SERVICE | 8083 | UP     |
| CROP-SERVICE       | 8084 | UP     |

---

## **🔄 System Workflow (End-to-End)**

1. Zone Service creates a zone and registers IoT device
2. Sensor Service fetches live data every 10 seconds
3. Sensor sends data to Automation Service
4. Automation Service fetches thresholds from Zone Service
5. Rule engine triggers actions:

    * Temp > max → TURN_FAN_ON
    * Temp < min → TURN_HEATER_ON
6. Logs stored and available via API

---

## **🌐 API Endpoints (Gateway - Port 8080)**

### **Zone Service**

* POST `/api/zones`
* GET `/api/zones/{id}`
* PUT `/api/zones/{id}`
* DELETE `/api/zones/{id}`

---

### **Sensor Service**

* GET `/api/sensors/latest`

---

### **Automation Service**

* POST `/api/automation/process`
* GET `/api/automation/logs`

---

### **Crop Service**

* POST `/api/crops`
* PUT `/api/crops/{id}/status`
* GET `/api/crops`

---

## **🔐 Security (JWT)**

* Implemented at API Gateway
* Requires:

  ```
  Authorization: Bearer <token>
  ```
* Invalid/missing token → **401 Unauthorized**
* Public endpoints:

    * `/auth/login`
    * `/auth/register`

---

## **🌍 External IoT Integration**

* Base URL:

  ```
  http://104.211.95.241:8080/api
  ```
* Sensor Service fetches live telemetry
* Zone Service registers devices

---

## **🧪 Testing**

1. Open Postman
2. Import:

   ```
   postman/agms-postman-collection.json
   ```
3. Test:

    * Direct services (8081–8084)
    * Gateway (8080 with JWT)

---

## **📁 Project Structure**

```
AGMS/
├── ConfigServer/
├── Eureka_Server/
├── Api_Gateway/
├── ZoneService/
├── SensorService/
├── AutomationService/
├── CropService/
├── postman/
├── docs/
└── README.md
```



