# Price Config Boundary Alignment Design

## Date

2026-03-30

## Goal

This design defines one focused boundary-alignment change for the price-config module.

The purpose of this round is not to add new pricing features.
The purpose is to restore a clean separation between:

- admin price-config management
- client-side price reading and fee estimation

## Source Of Truth

- Code is the source of truth.
- This round follows the existing pricing business model as-is.
- `frontend-admin` is the only valid management surface for price configuration.
- `frontend-client` should keep only end-user read and estimate capabilities.
- The backend should expose separate admin and client-facing entry points so the boundary is explicit in code rather than implied by convention.

## Scope

### In Scope

- split backend price-config entry points into admin-management and client-read groups
- keep admin CRUD/list/detail under `/admin/price-config`
- keep client read-only access under `/price-config/current` and `/price-config/estimate`
- align `frontend-admin` API/store/pages to the admin entry points
- remove `frontend-client` admin price-config routes and pages
- shrink `frontend-client` price-config API/store to read-only behavior
- sync OpenSpec and tracking docs after implementation

### Out Of Scope

- redesigning the pricing model
- adding time-of-use or dynamic pricing
- changing charging-record fee calculation rules
- adding approval workflows for price changes
- broad refactors outside the price-config chain

## Current State Summary

### Boundary Drift

The current codebase contains a split-brain price-config setup:

- `frontend-admin` already has the intended admin management flow
- `frontend-client` still contains `/admin/price-config` routes and admin pages
- backend price-config endpoints are grouped under one controller path, so admin and client behavior are mixed at the HTTP boundary

This means the code allows the client application to carry admin-facing management artifacts even though those responsibilities belong exclusively to `frontend-admin`.

### Architectural Problem

The current issue is not missing functionality.
The current issue is responsibility leakage.

Typical symptoms include:

- admin CRUD capabilities present in the client app
- shared frontend store patterns that mix management and read-only behavior
- backend route structure that does not clearly express who should call which endpoint

## Recommended Approach

### Approach 1: Frontend Cleanup Only

Remove the client admin routes and pages, but keep the backend controller shape unchanged.

Why it is not recommended:

- it fixes the symptom in the UI but leaves the backend boundary ambiguous
- future frontend work could easily drift back into mixed usage

### Approach 2: Split Entry Points, Recommended

Make the boundary explicit across both backend and frontend layers.

Backend:

- admin management endpoints move under `/admin/price-config`
- client read-only endpoints remain under `/price-config/current` and `/price-config/estimate`

Frontend:

- `frontend-admin` remains the only management surface
- `frontend-client` keeps only current-price lookup and fee estimation

Why this is recommended:

- it expresses responsibility clearly at the HTTP layer
- it matches the product boundary already established for other modules
- it keeps implementation scope focused without requiring a service-layer rewrite

### Approach 3: Full Service Split

Split controllers, services, stores, and types into completely separate admin/client trees.

Why it is not recommended in this round:

- the scope is too large for a boundary-cleanup change
- the existing service layer can remain shared for now without blocking clarity

## Design Decisions

### Decision 1: Backend Boundary Must Be Explicit

The backend should expose two clearly different surfaces:

- admin management surface: `/admin/price-config`
- client read surface: `/price-config/current`, `/price-config/estimate`

This keeps management semantics out of the client-facing route group.

### Decision 2: `frontend-admin` Owns All Price-Config Management

The admin app remains responsible for:

- list
- create
- edit
- delete
- activate/deactivate
- admin filtering and pagination

No management route or page should remain in `frontend-client` after this change.

### Decision 3: `frontend-client` Keeps Only Read And Estimate Behavior

The client app should retain only the capabilities that are directly needed by end users:

- fetch current effective price configuration
- estimate fee for a charging scenario
- display helper formatting around those results

The client price-config store should be reduced to this narrower responsibility.

### Decision 4: Service Layer Can Stay Shared For Now

This round does not need to split `PriceConfigService` unless a controller-level separation reveals a hard blocker.
The first goal is to make the boundary clear and enforceable without turning the work into a service refactor.

## File Boundaries

### Expected Backend Touch Points

- `backend/src/main/java/com/smartcharger/controller/PriceConfigController.java`
- possibly a new admin-facing controller for price-config management
- security or authorization wiring only if needed to protect the new admin routes

### Expected `frontend-admin` Touch Points

- `frontend-admin/src/api/priceConfig.ts`
- `frontend-admin/src/stores/priceConfig.ts`
- `frontend-admin/src/views/admin/PriceConfigList.vue`
- `frontend-admin/src/views/admin/PriceConfigForm.vue`
- `frontend-admin/src/router/index.ts` only if route naming or redirect cleanup is needed

### Expected `frontend-client` Touch Points

- `frontend-client/src/api/priceConfig.ts`
- `frontend-client/src/stores/priceConfig.ts`
- `frontend-client/src/router/index.ts`
- remove `frontend-client/src/views/admin/PriceConfigList.vue`
- remove `frontend-client/src/views/admin/PriceConfigForm.vue`

### Files To Avoid Touching

- charging-record fee-calculation internals
- unrelated admin/client modules
- broad type cleanups outside the price-config boundary

## Risks

### Risk 1: Client Read Features Could Be Accidentally Removed

The same client store currently covers both read-only and admin-style behavior.
A careless cleanup could break fee estimation or current-price display.

Mitigation:

- keep the client-side read API/store surface explicit before deleting admin artifacts
- validate all known consumer pages after the cleanup

### Risk 2: Admin Flow Could Break During Route Migration

If backend management routes move but `frontend-admin` is not updated in lockstep, admin CRUD will fail immediately.

Mitigation:

- migrate backend and admin API calls in the same change
- verify list/detail/create/edit/delete/toggle before touching client cleanup

### Risk 3: Old Documentation Will Drift Again

There are existing docs that describe the older mixed structure.
If the code is cleaned but the docs are not updated, the project will reintroduce the same confusion later.

Mitigation:

- update OpenSpec and module tracking docs as part of the same change

## Validation Focus

### Admin Validation

- price-config list loads
- pagination and filters still work
- create works
- edit works
- delete works
- activate/deactivate works

### Client Validation

- charging-pile detail still loads current pricing
- reservation detail still estimates fees correctly
- price display widgets still render current config and total unit price correctly

### Boundary Validation

- `frontend-admin` no longer calls admin CRUD through `/price-config`
- admin management uses `/admin/price-config`
- `frontend-client` no longer contains `/admin/price-config` routes or pages
- `frontend-client` price-config API/store no longer expose admin management methods

## Acceptance Standard

This change is complete when:

- admin price-config management is clearly isolated in `frontend-admin`
- client price-config behavior is read-only and estimate-only
- backend route structure makes the admin/client boundary explicit
- no client admin price-config pages or routes remain
- documentation reflects the cleaned boundary

## Recommended Next Step After Approval

1. write an OpenSpec change for `price-config-boundary-alignment`
2. create the implementation plan
3. migrate backend management routes
4. align `frontend-admin` to the new admin route group
5. remove client admin artifacts and shrink client price-config store/API
6. validate admin and client pricing flows
7. sync tracking docs