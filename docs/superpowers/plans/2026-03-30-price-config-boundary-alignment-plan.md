# Price Config Boundary Alignment Implementation Plan

## Scope

This plan covers only the `price-config-boundary-alignment` change.

Included:

- split backend price-config endpoints into admin-management and client-read entry points
- migrate admin CRUD/list/detail calls to `/admin/price-config`
- keep client price access limited to current-price lookup and fee estimation
- remove client-side admin price-config routes and pages
- shrink the client price-config API/store to read-only behavior
- update OpenSpec and project tracking docs after implementation

Excluded:

- redesigning the pricing model
- dynamic pricing or time-of-use pricing
- fee-calculation changes in charging-record flows
- unrelated frontend-client TypeScript cleanup
- broad service-layer refactors

## Objective

Restore a clean admin/client boundary for the price-config module so that:

- `frontend-admin` is the only management surface
- `frontend-client` only reads effective price data and estimates fees
- backend routes express that separation directly

## Source Of Truth

This plan follows the approved design spec:

- `docs/superpowers/specs/2026-03-30-price-config-boundary-alignment-design.md`

The current code baseline is:

- admin management currently implemented in `frontend-admin`
- client read usage currently needed by `ChargingPileDetail.vue`, `ReservationDetail.vue`, `PriceInfo.vue`, and `PriceEstimate.vue`
- backend service layer can remain shared during this round

## Deliverables

1. A focused OpenSpec change directory for `price-config-boundary-alignment`
2. Updated implementation plan and task tracking docs
3. Backend admin-facing price-config controller surface under `/admin/price-config`
4. Backend client-facing read-only price-config surface under `/price-config/current` and `/price-config/estimate`
5. Updated `frontend-admin/src/api/priceConfig.ts`
6. Updated `frontend-admin/src/stores/priceConfig.ts`
7. Shrunk `frontend-client/src/api/priceConfig.ts`
8. Shrunk `frontend-client/src/stores/priceConfig.ts`
9. Removed client admin price-config routes and pages
10. Updated module baseline and inventory notes

## Execution Strategy

### Phase 1: OpenSpec Setup

Goal:

- Create a small explicit change record for the boundary cleanup

Actions:

- create `openspec/changes/price-config-boundary-alignment/`
- add `.openspec.yaml`
- add `proposal.md`
- add `design.md`
- add `tasks.md`
- add a focused requirement spec under `specs/`

Completion criteria:

- the change exists and documents the approved boundary

### Phase 2: Backend Route Split

Goal:

- make the admin/client boundary explicit at the HTTP layer

Actions:

- move management endpoints to `/admin/price-config`
- keep only current-price lookup and estimate under `/price-config`
- leave `PriceConfigService` shared unless a hard blocker appears

Completion criteria:

- backend route groups clearly separate management from client reads

### Phase 3: Admin Alignment

Goal:

- ensure the admin app is the only management consumer

Actions:

- update `frontend-admin/src/api/priceConfig.ts` to use admin endpoints
- remove unused read-only helpers from the admin store if they are no longer needed
- keep existing admin pages working against the new route group

Completion criteria:

- admin list/create/edit/delete/toggle all target `/admin/price-config`

### Phase 4: Client Cleanup

Goal:

- remove management leakage from the client app without breaking user-facing price reads

Actions:

- remove `/admin/price-config` routes from `frontend-client/src/router/index.ts`
- remove client admin price-config pages
- reduce client API/store to `getCurrentPriceConfig` and `estimatePrice`
- preserve existing read-only consumers

Completion criteria:

- no admin management path remains in `frontend-client`
- user-facing price display and estimate flows still function

### Phase 5: Validation And Documentation Sync

Goal:

- verify the targeted boundary cleanup and record unrelated failures separately

Actions:

- run targeted backend compile validation
- run `frontend-admin` type-check
- run targeted search validation for removed client admin routes/pages
- update OpenSpec task state
- update module baseline and inventory notes

Completion criteria:

- targeted admin/client boundary cleanup is complete
- documentation reflects the cleaned state

## Validation

### Backend Validation

- admin CRUD/list/detail endpoints resolve under `/admin/price-config`
- current-price and estimate endpoints still resolve under `/price-config`
- backend compiles after controller split

### Admin Validation

- price-config list loads
- create works
- edit works
- delete works
- activate/deactivate works

### Client Validation

- `ChargingPileDetail.vue` can still estimate fees
- `ReservationDetail.vue` can still estimate fees
- `PriceInfo.vue` can still load current pricing
- `PriceEstimate.vue` can still load fee estimates

### Boundary Validation

- `frontend-client` contains no `/admin/price-config` routes
- `frontend-client` contains no admin price-config pages
- `frontend-client` price-config store exposes read-only behavior only

## Exit Condition

This plan is complete when:

- backend admin/client price-config routes are separated
- admin management is isolated to `frontend-admin`
- client price-config behavior is read-only and estimate-only
- tracking docs reflect the cleaned boundary