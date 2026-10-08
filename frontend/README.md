# AgriConnect Farm — Frontend

React (Vite) frontend covering every module of the backend: landing page,
auth, farms, crops/livestock, inventory, finance, marketplace, and orders
with Paystack payment initiation.

## Structure

```
src/
├── api/                     # one file per backend module (farmApi, financeApi, marketplaceApi, paymentApi, analyticsApi...)
├── features/auth/           # AuthContext, LoginPage, RegisterPage
├── components/
│   ├── AppLayout.jsx        # shared nav + header for every authenticated page
│   └── ProtectedRoute.jsx
├── pages/
│   ├── LandingPage.jsx
│   ├── DashboardPage.jsx    # real analytics summary + links to every module
│   ├── FarmsPage.jsx        # farm CRUD + inline weather lookup
│   ├── CropsLivestockPage.jsx
│   ├── InventoryPage.jsx
│   ├── FinancePage.jsx      # expenses, revenue, live profit calculation
│   ├── MarketplacePage.jsx  # public browse + seller listing management + place order
│   └── OrdersPage.jsx       # buyer payment flow, seller fulfill/release flow
├── utils/errors.js
└── styles/                  # auth.css, dashboard.css, landing.css, applayout.css
```

## Prerequisites

- Node.js 18+
- The backend running on `http://localhost:8080`

## Setup

```bash
npm install
cp .env.example .env
npm run dev
```

Opens on `http://localhost:5173`.

## Role-aware UI

The app reads the logged-in user's role (`OWNER`/`ADMIN`/`FARMER`/`BUYER`,
returned at login/register) and adapts:
- Only sellers (`OWNER`/`ADMIN`/`FARMER`) see "My Listings" and the create-listing
  form, and the fulfill/release actions on Orders.
- Only `BUYER` accounts see the order-quantity input on the Marketplace browse
  tab and the "Pay with Paystack" button on Orders.

## Payment flow (demo-mode note)

`OrdersPage` calls `POST /api/payments/initiate` and shows the returned
Paystack authorization URL directly in the page rather than redirecting —
this keeps the flow visible for a demo instead of leaving the app. In a real
deployment you'd `window.location.href = result.authorizationUrl` to send
the buyer to Paystack's hosted checkout.

## Known limitation from this build environment

Written and structurally reviewed (import paths verified, JSX brace-balanced
across all 18 files) without Node/npm in this sandbox, so `npm install` /
`npm run dev` couldn't be executed here. Run it locally first — paste back
any error and it gets fixed immediately.
