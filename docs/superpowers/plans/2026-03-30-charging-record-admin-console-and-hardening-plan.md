# Charging Record Admin Console And Hardening Implementation Plan

## Scope

This plan covers only the `charging-record-admin-console-and-hardening` change.

Included:

- audit the current charging-record backend against the approved design
- keep and document backend hardening that is already implemented in code
- fill only the remaining backend gaps needed for a stable admin console
- add a dedicated charging-record console in `frontend-admin`
- update OpenSpec and project baseline documents after implementation

Excluded:

- redesigning the client charging-record experience
- charging-payment or billing refactors
- reservation workflow refactors
- websocket or realtime monitoring
- repository-wide TypeScript cleanup unrelated to charging records

## Objective

Bring the charging-record module back into alignment with code reality and complete the missing admin operating surface.

The target result is:

- backend hardening is verified and only minimally extended where still missing
- admin gains a dedicated charging-record list and detail flow
- old charging-record docs no longer claim unfinished work that is already done

## Source Of Truth

This plan follows the approved design spec:

- `docs/superpowers/specs/2026-03-30-charging-record-admin-console-and-hardening-design.md`

The current codebase is authoritative.
Existing `openspec/changes/charging-record-frontend` is reference material only and must be corrected where it drifts from code.

## Current Code Reality

The audit before implementation already shows that several backend hardening items exist in code:

- `ChargingRecordServiceImpl.startCharging()` already uses Redisson user and pile locks
- `StartChargingTxService` already exists and handles the transactional start flow
- `getChargingRecordList()` already batch-fetches charging piles and vehicles
- `getAllChargingRecords()` already batch-fetches charging piles and vehicles
- controller pagination parameters already have basic validation

The main remaining product gap is on the admin frontend:

- no dedicated charging-record admin API module
- no dedicated charging-record store
- no dedicated charging-record list/detail pages
- no direct admin navigation entry for charging records

Possible backend follow-up work should therefore be limited to gaps uncovered by code inspection, such as:

- missing admin detail endpoint if the dedicated page needs it
- minor filter or response-shape corrections
- targeted cleanup of charging-record docs/tasks to match reality

## Deliverables

1. Implementation plan aligned to current code reality
2. Focused OpenSpec change directory for `charging-record-admin-console-and-hardening`
3. Backend charging-record audit notes reflected in code or docs
4. Any missing backend endpoint or service adjustment required by the admin console
5. New `frontend-admin` charging-record API/types/store
6. New `frontend-admin` charging-record list page
7. New `frontend-admin` charging-record detail page
8. Router and navigation integration in `frontend-admin`
9. Updated baseline and inventory documents

## Execution Strategy

### Phase 1: OpenSpec Setup

Goal:

- create a focused execution record for this round instead of relying on the stale old charging-record change

Actions:

- create `openspec/changes/charging-record-admin-console-and-hardening/`
- add `proposal.md`
- add `design.md`
- add `tasks.md`
- add a focused spec requirement file under `specs/`

Completion criteria:

- the new change directory exists and matches the approved scope

### Phase 2: Backend Audit And Gap Closure

Goal:

- verify what backend hardening is already present and implement only the remaining missing support needed by the admin console

Actions:

- inspect controller/service/repository code paths used by:
  - user list
  - user detail
  - admin all-records list
  - start charging
- compare actual code against the approved design
- preserve existing lock and batch-fetch logic
- add only missing backend support required by the admin detail experience or validation

Completion criteria:

- backend work is reduced to real gaps, not repeated implementation
- admin console has stable backend support

### Phase 3: Admin Data Layer

Goal:

- establish a clean dedicated admin-side charging-record data layer

Actions:

- add `frontend-admin/src/api/chargingRecord.ts`
- add `frontend-admin/src/types/chargingRecord.ts`
- add `frontend-admin/src/stores/chargingRecord.ts`
- model list and detail requests around the existing backend contract

Completion criteria:

- admin views do not embed request logic directly

### Phase 4: Admin Console UI

Goal:

- provide an independent operational charging-record console for administrators

Actions:

- add list page
- add detail page
- add filter controls
- add pagination
- integrate routes and top-level navigation

Minimum capabilities:

- list page:
  - record id
  - user id
  - charging pile id or pile code
  - status
  - start/end time
  - electric quantity
  - fee
  - filters
  - pagination
- detail page:
  - core identifiers
  - pile information
  - vehicle information when present
  - time and fee fields
  - created/updated timestamps

Completion criteria:

- administrators can independently search and inspect charging records from the admin app

### Phase 5: Documentation Sync

Goal:

- update tracking artifacts so they reflect real code rather than stale assumptions

Actions:

- update `openspec/changes/charging-record-admin-console-and-hardening/tasks.md`
- update old `openspec/changes/charging-record-frontend/tasks.md` only if needed to mark already-landed backend work as historical context
- update `接口文档设计/待实现功能清单.md`
- update `docs/superpowers/specs/2026-03-30-project-module-baseline.md`

Completion criteria:

- charging-record module state is readable from docs without contradicting current code

## Work Breakdown

### Task Group A: OpenSpec

1. Create the new change directory
2. Write proposal
3. Write design summary derived from the approved spec
4. Write executable tasks list
5. Add focused requirement spec

### Task Group B: Backend Audit

1. Confirm the existing locking and transaction split
2. Confirm batch-fetch coverage on list and admin-list paths
3. Identify whether admin detail needs a new backend endpoint
4. Add only the missing backend support

### Task Group C: Admin Data Layer

1. Define charging-record types for admin
2. Implement admin list and detail API calls
3. Implement store state for list/detail/filter/loading

### Task Group D: Admin UI

1. Build charging-record list page
2. Build charging-record detail page
3. Add route registration
4. Add app navigation entry
5. Check consistency with existing admin visual patterns

### Task Group E: Validation And Docs

1. Run static search for new charging-record admin routes and APIs
2. Attempt targeted type-check/build validation
3. Update OpenSpec task states
4. Update project inventory and baseline docs
5. Classify any remaining failures as pre-existing or newly introduced

## Validation

### Static Validation

- `frontend-admin` contains dedicated charging-record API, store, routes, and views
- charging-record navigation is reachable from the admin shell
- no unrelated module contracts are changed as part of this work

### Functional Validation

Backend:

1. user charging-record list still works
2. user charging-record detail still works
3. admin all-records list still works
4. if added, admin detail endpoint returns the expected record

Admin:

1. open charging-record list
2. filter by status
3. filter by user id if supported
4. filter by charging pile id if supported
5. filter by date range
6. paginate results
7. open detail page

### Build Validation

- attempt `npm run type-check` in `frontend-admin`
- if backend changes are made, run targeted backend verification where practical
- separate newly introduced problems from historical repository failures

## Risks

### Risk 1: Old Spec Drift

Mitigation:

- keep code as the baseline
- treat the old charging-record spec as historical context only

### Risk 2: Overbuilding Backend Work

Mitigation:

- do not rewrite existing locking or batch-fetch logic unless a real defect is found
- limit backend edits to concrete missing support for the admin console

### Risk 3: Admin Console Duplication

Mitigation:

- reuse charging-record response fields already exposed by the backend
- keep the new admin console simpler and more canonical than the existing user drawer view

### Risk 4: Validation Noise

Mitigation:

- validate the charging-record slice directly
- document unrelated repository failures instead of conflating them with this change

## Exit Condition

This plan is complete when:

- a focused charging-record OpenSpec change exists
- backend hardening is audited and only real gaps are addressed
- `frontend-admin` has an independent charging-record console
- the project baseline documents reflect the actual module state