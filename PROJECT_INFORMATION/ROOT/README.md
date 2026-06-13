# MyPay — E-Wallet System

Distributed microservice-based e-wallet with multi-currency support, shared expense collections, and real-time settlement.

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Docker Desktop | 4.x+ | Run the backend stack |
| Docker Compose | v2 (bundled with Docker Desktop) | Orchestrate services |
| Node.js | 20 LTS+ | Run the frontend dev server |
| npm | 9+ (bundled with Node.js) | Install frontend dependencies |
| Java 17 | JDK 17+ | Only needed if running backend locally without Docker |
| Maven | 3.9+ | Only needed if running backend locally without Docker |

Verify your tools:
```
docker --version
docker compose version
node --version
npm --version
```

---

## Project Structure

```
MyPay/
├── BACKEND/                  Spring Boot microservices
│   ├── docker-compose.yml    Full backend stack (recommended startup)
│   ├── init-schemas.sql      Database schema initialisation
│   ├── common-lib/           Shared DTOs, enums, exceptions
│   ├── config-server/        :8888  Centralised config
│   ├── discovery-server/     :8761  Eureka service registry
│   ├── api-gateway/          :8080  Single entry point for the frontend
│   ├── auth-service/         :8081
│   ├── wallet-service/       :8082
│   ├── collection-service/   :8083
│   ├── transaction-service/  :8084
│   ├── currency-service/     :8085
│   ├── notification-service/ :8086
│   └── reporting-service/    :8087
└── FRONTEND/
    └── mypay-frontend/       React 19 + Vite 8 mobile-web app
```

---

## Step 1 — Start the Backend (Docker Compose)

Open a terminal and navigate to the backend folder:

```
cd BACKEND
```

Build all service images and start the full stack:

```
docker compose up --build
```

> First run downloads base images and compiles all 11 Maven modules — expect **5–10 minutes**.
> Subsequent starts (without `--build`) take about 1–2 minutes.

### Startup order

Docker Compose respects health checks to start services in layers:

```
Layer 1  mysql · redis · rabbitmq          (infrastructure)
Layer 2  config-server                     (waits for mysql)
Layer 3  discovery-server                  (waits for config-server)
Layer 4  api-gateway                       (waits for discovery-server + redis)
Layer 5  auth · wallet · collection ·
         transaction · currency ·
         notification · reporting          (wait for discovery-server + mysql)
```

The stack is **ready** when you see log lines like:
```
mypay-auth-service        | Started AuthServiceApplication in X.Xs
mypay-wallet-service      | Started WalletServiceApplication in X.Xs
```

### Verify the backend is up

```
curl http://localhost:8080/actuator/health
```

Or open in a browser: `http://localhost:8761` — the Eureka dashboard should list all registered services.

---

## Step 2 — Start the Frontend

Open a **new** terminal (keep the Docker terminal running):

```
cd FRONTEND/mypay-frontend
```

Install dependencies (first time only):

```
npm install
```

Start the development server:

```
npm run dev
```

The app is available at: **http://localhost:5173**

> The frontend connects to the API gateway at `http://localhost:8080`.
> Make sure the backend is fully started before logging in.

---

## Step 3 — Log In

All seed accounts share the password **`Test@1234`**.

| Name | Email | Role in collections |
|------|-------|---------------------|
| Alice Tan | alice.tan@mypay.test | Admin (owns Bali Trip, member of Team Building) |
| Bob Lim | bob.lim@mypay.test | Admin (owns Office Lunch, SG Weekend) |
| Carol Wong | carol.wong@mypay.test | Admin (owns Team Building) |
| David Chen | david.chen@mypay.test | Admin (owns Holiday Dinner) |
| Emma Lee | emma.lee@mypay.test | Member only |
| Frank Ng | frank.ng@mypay.test | Member only |

> Seed data is loaded automatically on first startup (via the `dev` Spring profile).
> It is **idempotent** — restarting the backend will not duplicate records.

---

## Service Endpoints

| Service | URL |
|---------|-----|
| API Gateway (frontend entry point) | http://localhost:8080 |
| Eureka dashboard | http://localhost:8761 |
| Config Server | http://localhost:8888 |
| Auth Service | http://localhost:8081 |
| Wallet Service | http://localhost:8082 |
| Collection Service | http://localhost:8083 |
| Transaction Service | http://localhost:8084 |
| Currency Service | http://localhost:8085 |
| Notification Service | http://localhost:8086 |
| Reporting Service | http://localhost:8087 |
| RabbitMQ Management | http://localhost:15672 (guest / guest) |
| MySQL | localhost:**3307** (root / root) |

> MySQL is exposed on port **3307** (not 3306) to avoid conflicts with a local MySQL installation.

---

## Stopping the Backend

```
docker compose down
```

To also remove all data volumes (full reset):

```
docker compose down -v
```

---

## Running Without Docker (Local Development)

Use this method only if you need to debug a specific service in your IDE.

### Prerequisites
- Java 17 JDK
- Maven 3.9+
- Local MySQL 8 running on port 3306 with credentials `root / root`
- Local Redis running on port 6379
- Local RabbitMQ running on port 5672

### Step 1 — Install common-lib

```
cd BACKEND
mvn install -pl common-lib
```

### Step 2 — Start services in order

Start each service from its own folder. Each needs `SPRING_PROFILES_ACTIVE=dev` for seed data.

**1. Config Server**
```
cd config-server
mvn spring-boot:run
```
Wait until port 8888 is up, then:

**2. Discovery Server**
```
cd discovery-server
mvn spring-boot:run
```
Wait until Eureka is up at http://localhost:8761, then start the remaining services (each in its own terminal):

**3–9. Business Services** (order does not matter once Eureka is up)
```
cd auth-service        && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd wallet-service      && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd collection-service  && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd transaction-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd currency-service    && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd reporting-service   && mvn spring-boot:run
```

**10. API Gateway**
```
cd api-gateway && mvn spring-boot:run
```

---

## Troubleshooting

**Services fail to start / can't connect to MySQL**
- Wait for the `mysql` container to pass its health check before services start.
  Docker Compose handles this automatically with `condition: service_healthy`.
- If you restarted Docker mid-way, run `docker compose down` then `docker compose up`.

**`Host '...' is not allowed to connect` error**
- The MySQL container is configured with `MYSQL_ROOT_HOST: "%"` to allow all hosts.
  This is set in `docker-compose.yml`.

**Frontend shows network error / API calls fail**
- Confirm the backend is fully up: check http://localhost:8761 shows all services registered.
- Confirm the API gateway is running: `curl http://localhost:8080/actuator/health`.

**Port already in use**
- Check what is using the port: `netstat -ano | findstr :8080` (Windows)
- Stop conflicting processes or edit port mappings in `docker-compose.yml`.

**Seed data not loading**
- Seed data requires the `dev` Spring profile to be active.
  The `docker-compose.yml` already sets `SPRING_PROFILES_ACTIVE: docker,dev` for the relevant services.
- Check service logs: `docker compose logs auth-service | findstr Seed`

**Rebuild a single service after code change**
```
docker compose up --build auth-service
```
