# User Management Contract Alignment Implementation Plan

## Scope

This plan covers only the `user-management-contract-alignment` change.

Included:

- Align the active admin user-management flow to the current backend contract
- Use `frontend-admin/src/api/user.ts` and `frontend-admin/src/types/user.ts` as the primary contract
- Repair the active store and component chain:
  - `stores/userManagement.ts`
  - `views/admin/UserManagementList.vue`
  - `components/UserDetailDrawer.vue`
  - `components/BatchStatusDialog.vue`
  - `components/PasswordResetDialog.vue`
- Update OpenSpec and tracking documents after implementation

Excluded:

- Adding new user-management pages
- Backend API expansion
- Broad admin-wide type refactors
- Fixing unrelated announcement, statistics, or charging-pile TypeScript failures

## Objective

Restore one coherent admin user-management contract so the active page, store, and detail interactions all consume the same request and response shapes.

The target outcome is:

- list page uses backend user fields correctly
- single-user status updates submit `{ status, reason? }`
- batch status updates submit `{ userIds, status, reason? }`
- detail drawer tabs consume the real charging, reservation, and violation response models
- password reset dialog consumes the real user detail model

## Source Of Truth

This plan follows the approved design spec:

- `docs/superpowers/specs/2026-03-30-user-management-contract-alignment-design.md`

The backend contract is the baseline:

- `GET /admin/users`
- `GET /admin/users/{id}`
- `PUT /admin/users/{id}/status`
- `PUT /admin/users/{id}/reset-password`
- `GET /admin/users/{id}/charging-records`
- `GET /admin/users/{id}/reservations`
- `GET /admin/users/{id}/violations`
- `PUT /admin/users/batch-status`
- `GET /admin/users/export`

## Deliverables

1. A focused OpenSpec change directory for `user-management-contract-alignment`
2. Updated implementation plan and task tracking documents
3. Repaired `frontend-admin/src/types/user.ts`
4. Repaired `frontend-admin/src/stores/userManagement.ts`
5. Repaired `frontend-admin/src/views/admin/UserManagementList.vue`
6. Repaired `frontend-admin/src/components/UserDetailDrawer.vue`
7. Repaired `frontend-admin/src/components/BatchStatusDialog.vue`
8. Repaired `frontend-admin/src/components/PasswordResetDialog.vue`
9. Updated project baseline and inventory notes

## Execution Strategy

### Phase 1: OpenSpec Setup

Goal:

- Create a small, explicit change record for the contract repair

Actions:

- Create `openspec/changes/user-management-contract-alignment/`
- Add `.openspec.yaml`
- Add `proposal.md`
- Add `design.md`
- Add `tasks.md`
- Add a focused requirement spec under `specs/`

Completion criteria:

- The change exists and documents the approved boundaries

### Phase 2: Primary Type Alignment

Goal:

- Make `types/user.ts` reflect the backend fields actually returned to admin user-management views

Actions:

- Confirm the admin user response shape
- Confirm charging record, reservation, and violation item shapes
- Add any tiny helper types needed by the active UI chain
- Avoid broad refactors outside the active path

Completion criteria:

- The primary user-management types match the backend contract used by the store and components

### Phase 3: Store Alignment

Goal:

- Ensure the store is the single consistent consumer of the primary contract

Actions:

- Keep all active user-management requests on `api/user.ts`
- Normalize list pagination handling
- Normalize single-user status update and batch status update calls
- Make child-record fetch methods safe to call without ad hoc second arguments

Completion criteria:

- The store no longer mixes old and new payload shapes

### Phase 4: Active Component Alignment

Goal:

- Repair the active admin UI chain without adding new functionality

Actions:

- Update `UserManagementList.vue` to render real user fields
- Update status toggle flow to send the correct request object
- Update `BatchStatusDialog.vue` to send the real batch payload
- Update `UserDetailDrawer.vue` to render real detail fields and child-table models
- Update `PasswordResetDialog.vue` to consume the real user detail model

Completion criteria:

- The active page, drawer, and dialogs all compile against the same contract

### Phase 5: Validation And Documentation Sync

Goal:

- Verify the targeted user-management repair and record remaining repository-level issues separately

Actions:

- Run `npm.cmd run type-check` in `frontend-admin`
- Confirm user-management-specific errors are removed
- Record remaining non-user-management failures as historical issues
- Update OpenSpec task state
- Update project baseline and inventory notes

Completion criteria:

- User-management drift errors are cleared from type-check output
- Tracking docs reflect that this module is contract-aligned again

## Validation

### Functional Validation

- user list loads and paginates
- single-user status toggle submits successfully
- batch status dialog submits successfully
- password reset dialog still resets and displays a generated password
- user detail drawer loads user detail
- charging record tab loads
- reservation tab loads
- violation tab loads

### Type Validation

The target is to remove the current user-management-specific `frontend-admin` type-check failures.
Unrelated failures may remain and must be recorded as pre-existing.

## Exit Condition

This plan is complete when:

- the active admin user-management flow uses one primary API/type contract
- store and component payload shapes are consistent with the backend
- user-management-specific type-check failures are removed
- OpenSpec and tracking docs reflect the repaired state