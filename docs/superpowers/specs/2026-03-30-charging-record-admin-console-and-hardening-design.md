# Charging Record Admin Console And Hardening Design

## Date

2026-03-30

## Goal

This design covers one narrow module-strengthening change for charging records.
The work combines two related parts inside the same module:

1. backend hardening for charging-record query and start-charging safety
2. a dedicated admin charging-record console in `frontend-admin`

The purpose is not to redesign the charging-record business model.
The purpose is to stabilize the existing backend capability and expose a proper admin operating surface.

## Source Of Truth

- Code is the source of truth.
- Existing old docs may be stale.
- Existing `openspec/changes/charging-record-frontend` contains useful context but should not be treated as the exact execution plan for this round.

## Scope

### In Scope

- backend charging-record query-path optimization
- backend concurrency protection for `startCharging`
- dedicated admin charging-record list page
- dedicated admin charging-record detail page
- admin filters and pagination
- admin router and navigation entry
- OpenSpec and baseline doc sync

### Out Of Scope

- client charging-record UX redesign
- payment or billing redesign
- reservation redesign
- websocket or realtime dashboard work
- large repository-wide TypeScript cleanup
- deep data-model refactor across unrelated modules

## Current State Summary

### Backend

The backend already exposes charging-record capability under `/charging-record`.
Important existing endpoints include:

- user list
- user detail
- current charging record
- monthly statistics
- yearly statistics
- admin all-records list under `/charging-record/admin/all`

This means the backend already has the right module boundary, but still needs strengthening around performance and concurrency behavior.

### Frontend Admin

`frontend-admin` does not currently have a dedicated charging-record console.
The admin side only has indirect access through user-related views and drawers.
That is insufficient for independent operational search and troubleshooting.

### Frontend Client

`frontend-client` already has charging-record list, detail, and statistics pages.
Those pages are not the target of this change unless a backend adjustment requires minimal compatibility work.

## Design Strategy

This change is implemented in two ordered phases inside one module change.

### Phase A: Backend Hardening

This phase makes the backend trustworthy enough for an admin console to depend on.

Includes:

- query-path review and optimization
- `startCharging` concurrency protection
- minimal parameter validation cleanup where required
- targeted verification for backend behavior

### Phase B: Admin Console

This phase adds the missing independent admin surface.

Includes:

- admin charging-record api/types/store
- admin charging-record list page
- admin charging-record detail page
- filters, pagination, router, and navigation

## Backend Design

### 1. Query Optimization Goal

The existing implementation should be reviewed for repeated repository fetch patterns when composing charging-record responses.
The target is not premature micro-optimization.
The target is to eliminate obvious repeated lookups and make the admin all-records path stable under normal operational usage.

Planned direction:

- inspect `ChargingRecordServiceImpl` list/detail/admin-all flows
- consolidate repeated associated-entity loading where possible
- prefer bounded, readable service-layer improvements over broad entity-model refactors
- keep existing response contracts stable unless a small correction is necessary

### 2. Concurrency Protection Goal

`startCharging` should not allow duplicate active charging records caused by closely concurrent requests.
Protection should cover at least:

- same user issuing repeated start requests
- same charging pile being started concurrently

Planned direction:

- add explicit concurrency control around start-charging critical section
- keep transaction boundaries coherent
- avoid introducing cross-module side effects
- preserve current success/failure semantics as much as possible

### 3. Backend Non-Goals

This round should avoid:

- redesigning the full charging lifecycle
- replacing the whole persistence model
- turning the module into an event-driven workflow
- broad API contract churn

## Admin Console Design

### 1. List Page

The admin list page should become the main operational entry.

Required content:

- charging record id
- user information
- charging pile information
- start time
- end time
- electric quantity
- fee
- status
- action entry to detail page

Required controls:

- status filter
- user filter if backend contract already supports it cleanly
- charging pile filter if backend contract already supports it cleanly
- time range filter if backend contract already supports it cleanly
- pagination

The page should be good for troubleshooting first, not analytics-first.

### 2. Detail Page

The admin detail page should expose the full record needed for inspection.

Should include:

- core record identifiers
- user information
- pile information
- vehicle information if backend response already includes it
- charging timing
- fee and quantity fields
- status fields
- created and updated timestamps

This page is read-oriented.
Unless there is already a safe backend admin operation, this round should not add record mutation controls.

### 3. Entry Integration

`frontend-admin` should gain:

- route for charging-record list
- route for charging-record detail
- navigation link in app shell

The admin console should be reachable without going through user management.

## Change Shape

Recommended OpenSpec change name:

- `charging-record-admin-console-and-hardening`

Reason:

- it accurately captures both the backend strengthening and the admin console addition
- it remains one module-focused change instead of turning into a cross-module package

## Expected Files

### Backend

Likely touch points:

- `backend/src/main/java/com/smartcharger/controller/ChargingRecordController.java`
- `backend/src/main/java/com/smartcharger/service/impl/ChargingRecordServiceImpl.java`
- `backend/src/main/java/com/smartcharger/repository/ChargingRecordRepository.java`
- possibly a new service class for transactional or locking separation
- possibly related DTO or result-code files if a minimal addition is needed

### Frontend Admin

Likely additions:

- `frontend-admin/src/api/chargingRecord.ts`
- `frontend-admin/src/types/chargingRecord.ts`
- `frontend-admin/src/stores/chargingRecord.ts`
- `frontend-admin/src/views/admin/ChargingRecordList.vue`
- `frontend-admin/src/views/admin/ChargingRecordDetail.vue`
- router integration
- app navigation integration

### Docs

- new OpenSpec change files
- update `接口文档设计/待实现功能清单.md`
- update `docs/superpowers/specs/2026-03-30-project-module-baseline.md`

## Risks

### 1. Old Spec Drift

The existing charging-record spec material includes backend optimization ideas that may not exactly match current code.
We should reuse intent, not blindly reuse the old implementation plan.

### 2. Concurrency Change Risk

If locking or transaction boundaries are too coarse, normal charging start flow may degrade or deadlock.
This must be implemented conservatively.

### 3. Admin/UI Drift Risk

The admin console should not duplicate inconsistent logic already embedded in user-management drawers.
It should become the canonical admin entry instead of creating a second divergent pattern.

### 4. Validation Noise

The repository still has historical TypeScript issues in unrelated modules.
Validation for this change must separate newly introduced problems from pre-existing failures.

## Verification Focus

### Backend

- user-side charging-record list still works
- user-side charging-record detail still works
- admin all-records endpoint still works
- same-user concurrent `startCharging` requests do not create duplicate active records
- same-pile concurrent starts do not create conflicting active records

### Frontend Admin

- list page loads correctly
- filters and pagination work with backend contract
- detail page loads correctly
- navigation entry is reachable and consistent

### Regression

- client charging-record pages do not regress
- existing user-management drawer behavior is not broken by backend changes

## Acceptance Standard

This change is complete when:

- backend charging-record query path is materially hardened
- `startCharging` has explicit concurrency protection
- admin has an independent charging-record console
- docs and OpenSpec are synced to the new code reality
- any remaining verification failures are clearly classified as repository pre-existing issues if unrelated

## Recommended Next Step After Approval

1. write OpenSpec change files for `charging-record-admin-console-and-hardening`
2. implement backend hardening first
3. implement admin console second
4. run targeted validation
5. sync project inventory docs