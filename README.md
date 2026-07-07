# ⚡ Volt-Grid — EV Charging Station Network

A full-stack platform for operating a network of EV charging stations. Stations
report status and meter readings over **MQTT**; the backend tracks availability,
opens/closes **charging sessions**, prices energy, and exposes reservations and
revenue. A **React** dashboard shows the live fleet, and operators can push
remote **start/stop** commands back to any station over MQTT.

## Stack

| Layer     | Technology                                                        |
|-----------|-------------------------------------------------------------------|
| Backend   | Java 17, Spring Boot 3.2, Spring Data JPA, Spring Integration MQTT |
| Database  | PostgreSQL 16 + Flyway migrations                                 |
| Cache     | Redis 7 (station availability read cache)                        |
| Messaging | MQTT (Eclipse Mosquitto 2)                                        |
| Frontend  | React 18 + Vite + TypeScript                                      |
| DevOps    | Docker, docker-compose, Kubernetes                               |
| CI/CD     | GitHub Actions (build, test, image push to GHCR)                 |
| Tests     | JUnit 5 + Mockito + MockMvc (backend), Vitest + Testing Library (frontend) |

## How it works

```
 Station ──MQTT status/<id>──▶ Mosquitto ──▶ Backend (Spring Integration)
   {"status":"CHARGING",                        │  ├── opens ChargingSession
    "energyKwh":12.5}                           │  ├── PostgreSQL (stations, charging_sessions)
                                                │  └── Redis (availability cache)
 Station ◀─MQTT commands/<id>── Mosquitto ◀──── Backend  (START / STOP)
                                                ▲
 React dashboard ──REST /api/v1─────────────────┘  (list, reserve, sessions, revenue)
```

**Session lifecycle:** when a station reports `CHARGING`, the backend opens an
`ACTIVE` session and tracks its energy. When it reports any other status, the
active session is finalized and its cost is computed as `energyKwh × pricePerKwh`.

## Run everything (Docker)

```bash
docker compose up --build
```

- Dashboard: http://localhost:3000
- API: http://localhost:8080/api/v1/stations
- Health: http://localhost:8080/actuator/health
- MQTT broker: `tcp://localhost:1883`

## Local development

**Backend** (start infra first: `docker compose up postgres redis mosquitto`):

```bash
cd backend
mvn spring-boot:run
```

**Frontend:**

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173, proxies /api to :8080
```

## API

| Method | Path                                    | Purpose                          |
|--------|-----------------------------------------|----------------------------------|
| GET    | `/api/v1/stations`                      | List stations                    |
| POST   | `/api/v1/stations`                      | Register a station               |
| GET    | `/api/v1/stations/{id}`                 | Get one station (Redis-cached)   |
| POST   | `/api/v1/stations/{id}/reserve`         | Reserve an available station     |
| POST   | `/api/v1/stations/{id}/release`         | Release a reservation            |
| GET    | `/api/v1/stations/{id}/sessions`        | Recent charging sessions         |
| GET    | `/api/v1/stations/{id}/revenue`         | Total revenue for a station      |
| POST   | `/api/v1/stations/{id}/commands`        | Push START/STOP over MQTT        |
| POST   | `/api/v1/ingest/{id}`                   | HTTP status ingestion (non-MQTT) |

## Try a charging session over MQTT

Stations auto-register on first report. Simulate a session on `station-1`:

```bash
CID=$(docker ps -qf name=mosquitto)

# Start charging
docker exec -it $CID mosquitto_pub -t status/station-1 \
  -m '{"status":"CHARGING","energyKwh":0}'

# ... meter climbs ...
docker exec -it $CID mosquitto_pub -t status/station-1 \
  -m '{"status":"CHARGING","energyKwh":8.4}'

# Unplug — station goes available, session is finalized & priced
docker exec -it $CID mosquitto_pub -t status/station-1 \
  -m '{"status":"AVAILABLE","energyKwh":8.4}'
```

Then check `GET /api/v1/stations/station-1/sessions` and `.../revenue`.

## Tests

```bash
cd backend  && mvn test      # service, web-layer (MockMvc) and JPA slice tests
cd frontend && npm test      # component + form tests (Vitest)
```

## Kubernetes

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/            # infra + backend + frontend + ingress
```

Replace `ghcr.io/OWNER/...` images (CI fills `github.repository_owner`
automatically). Backend runs 2 replicas with a zero-downtime rolling update;
blue/green and canary are supported per the platform deploy runbooks.

## Production hardening

- Mosquitto uses `allow_anonymous true` for local dev — enable auth + TLS (port 8883) in production.
- Postgres/Redis run in-cluster for the demo; use managed services in production.
- Add authentication/authorization (e.g. OAuth2 resource server) before exposing the API.
- Payment capture for sessions is out of scope here — integrate a PSP against the `charging_sessions` cost.
