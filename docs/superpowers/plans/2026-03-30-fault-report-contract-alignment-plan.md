# Fault Report Contract Alignment Implementation Plan

## Scope

This plan covers only the `fault-report-contract-alignment` change.

Included:

- Align the client-side fault report contract to the existing backend
- Remove client-side dependencies on unsupported fault report fields
- Add admin-side fault report management pages
- Add admin-side fault report statistics page
- Update OpenSpec and baseline documents after implementation

Excluded:

- Backend expansion for fault type taxonomy
- Image upload support
- Extended processing timeline features
- Cross-module refactors unrelated to fault report

## Objective

Restore a real end-to-end fault report workflow using the backend as the source of truth.

The target closed loop is:

- Client: create, list, detail, cancel
- Admin: list, filter, paginate, detail, handle, statistics

## Source of Truth

This plan follows the approved design spec:

- `docs/superpowers/specs/2026-03-30-fault-report-contract-alignment-design.md`

The backend contract is the baseline:

- `POST /fault-report`
- `GET /fault-report`
- `GET /fault-report/{id}`
- `DELETE /fault-report/{id}`
- `GET /fault-report/admin/all`
- `GET /fault-report/admin/{id}`
- `PUT /fault-report/admin/{id}/handle`
- `GET /fault-report/admin/statistics`

## Deliverables

1. Updated client-side fault report API
2. Updated client-side fault report types
3. Updated client-side fault report store
4. Updated client-side fault report submission, list, and detail pages
5. New admin-side fault report API and store
6. New admin-side list page
7. New admin-side detail and handling page
8. New admin-side statistics page
9. New admin-side routes and menu entry if applicable
10. New OpenSpec change directory for `fault-report-contract-alignment`
11. Updated baseline and project inventory documents

## Execution Strategy

### Phase 1: OpenSpec Setup

Goal:

- Create a focused change record before code edits expand

Actions:

- Create `openspec/changes/fault-report-contract-alignment/`
- Add `proposal.md`
- Add `design.md`
- Add `tasks.md`
- Add fault report contract spec under `specs/`

Completion criteria:

- The change exists and describes the approved design boundaries

### Phase 2: Client Contract Correction

Goal:

- Make the client frontend consume only the real backend fault report contract

Actions:

- Rewrite `frontend-client/src/api/faultReport.ts`
- Remove unsupported endpoints
- Redefine the client-side response/request types
- Convert pagination handling from the current fake structure to backend `Page`

Completion criteria:

- The client API file contains only backend-supported endpoints
- The client types no longer define unsupported fields as required data

### Phase 3: Client Store Simplification

Goal:

- Remove store capabilities that depend on unsupported backend features

Actions:

- Keep only:
  - `createReport`
  - `fetchMyReportList`
  - `fetchReportDetail`
  - `removeReport`
  - `reset`
- Remove:
  - `updateReport`
  - `uploadImage`
  - `fetchPileStats`
- Update list and detail state assignment to match the corrected types

Completion criteria:

- The store reflects the actual client closed loop only

### Phase 4: Client UI Alignment

Goal:

- Update the client UI so it no longer depends on unsupported fault report fields

Actions:

- Update `ChargingPileDetail.vue`
  - remove fault type selector
  - remove image upload UI
  - submit only `chargingPileId + description`
- Update `FaultReportList.vue`
  - remove fault type filtering
  - remove image preview display
  - remove fault-type-driven icon logic
  - keep status filter and pagination
- Update `FaultReportDetail.vue`
  - remove fault type, image, and pseudo-timeline sections
  - show only real backend fields
  - allow cancel only in `PENDING`

Completion criteria:

- The client user journey matches the real backend data model

### Phase 5: Admin API and Store

Goal:

- Create the admin-side data access layer for fault report management

Actions:

- Add `frontend-admin/src/api/faultReport.ts`
- Add `frontend-admin/src/stores/faultReport.ts`
- Model list, detail, handle, and statistics requests around the existing backend DTOs

Completion criteria:

- Admin pages can be built without duplicating request logic inside views

### Phase 6: Admin Pages

Goal:

- Establish a real admin-side fault report management closed loop

Actions:

- Add list page
- Add detail and handling page
- Add statistics page
- Follow existing admin module conventions for layout and interaction patterns

Minimum capabilities:

- List page
  - status filter
  - charging pile filter
  - date range filter
  - pagination
- Detail page
  - report information
  - user information
  - pile information
  - handle status and remark
- Statistics page
  - total count
  - counts by status
  - average handling time
  - top fault piles

Completion criteria:

- Admin can review and handle fault reports end to end from the frontend

### Phase 7: Admin Routing Integration

Goal:

- Expose the new admin module through the admin application only

Actions:

- Add routes under `frontend-admin`
- Add navigation entry if the app uses route-driven menu configuration
- Confirm the client app does not receive any admin fault report entry

Completion criteria:

- Fault report management is reachable only from admin

### Phase 8: Documentation Sync

Goal:

- Bring project tracking documents in line with the actual implementation

Actions:

- Update `openspec/changes/fault-report-contract-alignment/tasks.md`
- Update `接口文档设计/待实现功能清单.md`
- Update `docs/superpowers/specs/2026-03-30-project-module-baseline.md`

Completion criteria:

- Fault report is no longer marked as a contract-broken module once the implementation is complete

## Work Breakdown

### Task Group A: OpenSpec

1. Create change directory
2. Write proposal
3. Write design summary derived from approved spec
4. Write executable tasks list
5. Write focused spec requirement file

### Task Group B: Client Contract

1. Replace `/api/fault-reports` endpoints with `/fault-report`
2. Remove `/my`, `/upload`, `/stats/pile/*`, and update endpoint assumptions
3. Update create request type to `chargingPileId + description`
4. Redefine list response to backend page structure
5. Redefine detail model to backend response structure

### Task Group C: Client State and Views

1. Simplify store methods
2. Update charging pile detail report dialog
3. Update my fault report list
4. Update fault report detail page
5. Remove UI references to unsupported fields

### Task Group D: Admin Data Layer

1. Add admin API file
2. Add admin type definitions if needed
3. Add admin store for list/detail/handle/statistics

### Task Group E: Admin UI

1. Build list page
2. Build detail and handling page
3. Build statistics page
4. Add routing integration
5. Verify admin-only exposure

### Task Group F: Documentation and Validation

1. Update OpenSpec task states
2. Update project inventory document
3. Update module baseline document
4. Run static searches for unsupported endpoints
5. Attempt type-check/build validation and record any pre-existing blockers

## Validation

### Static Validation

- No remaining `/api/fault-reports` usage in active source files
- No remaining client-side upload or fault-type request dependency in fault report flow
- Admin fault report pages and routes exist only in `frontend-admin`

### Functional Validation

Client:

1. Submit fault report from charging pile detail
2. View personal report list
3. Open detail page
4. Cancel a pending report

Admin:

1. Open fault report list
2. Filter by status
3. Filter by charging pile
4. Filter by date range
5. Paginate results
6. Open detail page
7. Submit handling status and remark
8. Open statistics page

### Build Validation

- Attempt `type-check` and build for `frontend-client`
- Attempt `type-check` and build for `frontend-admin`
- Separate newly introduced errors from historical repository errors

## Risks

### Risk 1: Client views rely on unsupported fields deeply

Mitigation:

- Correct types first
- Then update store
- Then update views in dependency order

### Risk 2: Admin patterns may vary across modules

Mitigation:

- Follow existing admin page conventions from current list/detail/statistics modules
- Keep the first version minimal and consistent

### Risk 3: Backend pagination or filter semantics may not exactly match current frontend assumptions

Mitigation:

- Read controller and service behavior before finalizing page state logic
- Treat backend semantics as authoritative

### Risk 4: Existing repository TypeScript errors may obscure new issues

Mitigation:

- Use targeted grep checks alongside build attempts
- Record historical errors explicitly instead of treating them as regressions

## Exit Condition

This plan is complete when:

- The fault report client flow uses only real backend fields and endpoints
- The admin flow can list, inspect, handle, and statistically review fault reports
- OpenSpec for `fault-report-contract-alignment` exists and tracks execution
- Baseline documentation reflects the repaired module state