# AgriConnect Farm

Multi-tenant farm management and produce marketplace SaaS — MMS 4 final year project.

```
agriconnect-farm-project/
├── backend/            Spring Boot API — see backend/README.md
├── frontend/           React + Vite SPA — see frontend/README.md
└── docker-compose.yml  Runs Postgres + backend + frontend together
```

## Feature completeness

Every module from the original project brief is implemented end-to-end
(backend API + frontend UI), not just auth:

| Module | Backend | Frontend |
|---|---|---|
| Multi-tenant auth (register/login/JWT) | ✅ | ✅ Landing → Register/Login → Dashboard |
| Farm management | ✅ | ✅ Farms page |
| Crop & livestock tracking | ✅ | ✅ Crops & Livestock page |
| Weather insights per farm | ✅ (OpenWeatherMap) | ✅ Inline on Farms page |
| Inventory & yield tracking | ✅ | ✅ Inventory page |
| Expense & revenue tracking | ✅ | ✅ Finance page with live profit calc |
| Marketplace (cross-tenant listings) | ✅ | ✅ Marketplace page (browse + sell) |
| Orders | ✅ | ✅ Orders page |
| Escrow payments (Paystack) | ✅ (real API client + webhook) | ✅ Pay/fulfill/release flow |
| Analytics dashboard | ✅ | ✅ Real numbers on Dashboard |

## Architecture highlight: two-tier multi-tenancy

Most data (Farm, Crop, Livestock, Inventory, Finance) is strictly isolated
per tenant via a Hibernate row-level filter — invisible to application code.
Marketplace data (Listing, Order) is a deliberate exception: it's designed to
be visible **across** tenants, with ownership checked explicitly instead of
filtered automatically. Both patterns are documented in code comments in
`backend/src/main/java/com/agriconnect/marketplace/` — worth highlighting in
your project defense as evidence of deliberate design, not an oversight.

## Running everything at once

```bash
docker-compose up --build
```

| Service | Port |
|---|---|
| `postgres` | 5432 |
| `backend` | 8080 |
| `frontend` | 5173 |

Open `http://localhost:5173`.

## External services

Two features need real API keys to actually function (see `backend/README.md`
for where to set them): **Paystack** (payments) and **OpenWeatherMap**
(weather). Everything else works with zero external dependencies beyond
Postgres.

## Running each half separately

```bash
# backend
cd backend && mvn spring-boot:run
cd backend && mvn clean test

# frontend
cd frontend && npm install && cp .env.example .env && npm run dev
```

## Known limitation from this build environment

The project was originally written in a sandbox without Maven, Node, or
internet access. It has since been built and verified on a real machine
(Java 26, Maven 3.9, Node 26, PostgreSQL 16):

- `mvn clean test` — **25 tests pass**
- `npm install && npm run build` — **passes**
- Full API smoke test (register → farm → crop → inventory → finance →
  marketplace listing → order → analytics) — **passes**

Two fixes were applied to make this work on **Java 26** (see `backend/pom.xml`):
Mockito was upgraded to 5.24.0 and ByteBuddy to 1.17.7 (the versions bundled
with Spring Boot 3.3.4 cannot instrument Java 26 class files). A
`TenantFilterInterceptor` ordering fix was also applied so the Hibernate
tenant filter runs after Spring's OpenEntityManagerInView interceptor.

## Registering as a BUYER

Registration now asks whether you're joining as a **Farmer / Cooperative**
(OWNER) or a **Buyer / Market trader** (BUYER). OWNER remains the default.
ADMIN and FARMER roles cannot be self-assigned at sign-up.

## Suggested demo script

Register an org as OWNER → add a farm → add a crop → check its weather →
record inventory and an expense → create a marketplace listing → register a
second org as BUYER (separate browser/incognito) → browse and order the
listing → pay → back in the OWNER account, mark fulfilled and release funds →
check the Dashboard numbers update throughout. This single flow demonstrates
multi-tenancy, RBAC, CRUD, cross-tenant marketplace logic, external API
integration, and payments in one pass — the exact "golden workflow" from the
original project pitch.
