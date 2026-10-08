# AgriConnect Farm — Backend

Multi-tenant farm management and produce marketplace SaaS API (MMS 4 final year project).

Every feature from the original project brief is implemented: multi-tenancy,
farm/crop/livestock management, inventory and yield tracking, expense/revenue
tracking, a cross-tenant marketplace, Paystack escrow payments, live weather
lookups, and an analytics dashboard.

## Module map

```
com.agriconnect
├── common/
│   ├── base/BaseEntity.java              # every tenant-scoped entity extends this
│   ├── exception/                        # GlobalExceptionHandler, NotFoundException
│   └── security/SecurityUtils.java       # current tenant/user/role + role-guard helper
├── tenant/                                # TenantContext (ThreadLocal) + Hibernate filter interceptor
├── auth/                                  # Organization (tenant), User, Role, JWT, register/login
├── farm/                                  # Farm, Crop, Livestock — full CRUD, tenant-filtered
├── inventory/                             # InventoryItem, YieldRecord
├── finance/                               # Expense, Revenue — with tenant-wide totals
├── marketplace/                           # Listing, Order — deliberately NOT tenant-filtered (see below)
├── payment/                               # EscrowTransaction, PaystackClient, webhook handling
├── weather/                               # WeatherClient (OpenWeatherMap), per-farm lookup
├── analytics/                             # DashboardSummary — aggregates every module above
└── config/                                # Security, CORS, JWT filter, web config
```

## Multi-tenancy: the one deliberate exception

Farm, Crop, Livestock, InventoryItem, YieldRecord, Expense, and Revenue all
extend `BaseEntity`, which carries a Hibernate `@Filter` restricting every
query to `tenant_id = :tenantId` — enabled automatically per request by
`TenantFilterInterceptor`. This is invisible to service code: `repository.findAll()`
is already tenant-scoped.

**Listing and Order do not extend `BaseEntity`.** A marketplace has to be
browsable across every tenant — a buyer in one organization needs to see
produce listed by farms in a completely different organization. Blanket
tenant filtering would break that by design. Instead:
- `Listing` carries `organizationId` (the seller's tenant) for ownership.
- `Order` carries both `buyerOrganizationId` and `sellerOrganizationId`.
- Ownership is checked explicitly in `MarketplaceService`/`EscrowService`
  (a plain equality check against `SecurityUtils.currentTenantId()`), not by
  a row-level filter.

This is worth stating plainly in your project defense: **multi-tenancy
isolates private operational data, not the public surface built on top of it.**

## Role-based access

Four roles: `OWNER`, `ADMIN`, `FARMER`, `BUYER`. `SecurityUtils.requireRole(...)`
guards mutating operations — e.g. only `OWNER`/`ADMIN`/`FARMER` can create a
farm or a listing; only `BUYER` can place an order. Violations return `403`
via `GlobalExceptionHandler`.

## API surface

| Module | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Farms | `GET/POST /api/farms`, `PUT/DELETE /api/farms/{id}` |
| Crops | `GET/POST /api/crops`, `PUT/DELETE /api/crops/{id}` |
| Livestock | `GET/POST /api/livestock`, `PUT/DELETE /api/livestock/{id}` |
| Weather | `GET /api/farms/{id}/weather` |
| Inventory | `GET/POST /api/inventory`, `PUT/DELETE /api/inventory/{id}` |
| Yield | `GET/POST /api/yield-records`, `DELETE /api/yield-records/{id}` |
| Finance | `GET/POST /api/finance/expenses`, `GET/POST /api/finance/revenues`, `DELETE .../{id}` |
| Marketplace | `GET /api/marketplace/listings` (public), `POST .../listings`, `GET .../listings/mine`, `DELETE .../listings/{id}` |
| Orders | `POST /api/marketplace/orders`, `GET .../orders/mine`, `GET .../orders/against-my-listings`, `POST .../orders/{id}/fulfill` |
| Payments | `POST /api/payments/initiate`, `POST /api/payments/webhook` (public), `POST /api/payments/orders/{id}/release` |
| Analytics | `GET /api/analytics/dashboard` |

## External services (need real keys to actually work)

- **Paystack** — set `PAYSTACK_SECRET_KEY`. Get a free test-mode key at paystack.com.
- **OpenWeatherMap** — set `WEATHER_API_KEY`. Free tier key at openweathermap.org.

Both clients (`PaystackClient`, `WeatherClient`) are written against the real
APIs using Java's built-in `HttpClient` — no extra dependency — but this
sandbox has no internet access, so they were reviewed for correctness but not
exercised against the live services. Test with real test-mode keys before
your demo.

## Prerequisites

- Java 21 or newer (verified on Java 26), Maven 3.9+
- PostgreSQL 16 (or use Docker — see the project root `README.md`)

## Running locally

```bash
# from the project root
docker-compose up -d postgres

# from this backend/ folder
mvn spring-boot:run
```

Or the whole stack from the project root: `docker-compose up --build`.

## Running the tests

```bash
mvn clean test
```

12 tests from the original auth module, plus:

| Test class | Covers |
|---|---|
| `FarmServiceTest` | Role-guarded create, rejection for wrong role, not-found lookup |
| `MarketplaceServiceTest` | Listing creation, order total calculation, insufficient-stock rejection, sold-out transition |
| `PaystackClientTest` | HMAC-SHA512 webhook signature verification (valid / tampered / missing) |
| `AnalyticsServiceTest` | Cross-module aggregation and net-profit math |

## Build verification

`mvn clean test` runs **25 tests** and passes. Two compatibility fixes are in
`pom.xml` for Java 26: Mockito is pinned to 5.24.0 and ByteBuddy to 1.17.7.
`WebConfig` also registers the tenant interceptor after Spring's
OpenEntityManagerInView interceptor so the Hibernate tenant filter applies to
the request-scoped EntityManager.
