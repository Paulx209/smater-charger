# User Management Contract Alignment Design

## Date

2026-03-30

## Goal

This design defines one narrow alignment change for the admin user-management module.

The purpose is not to add new user-management features.
The purpose is to restore a single coherent contract across the existing admin user-management chain:

- API layer
- type definitions
- store
- user list page
- user detail drawer
- batch status dialog

## Source Of Truth

- Code is the source of truth.
- This round follows the existing backend contract as-is.
- This round treats `frontend-admin/src/api/user.ts` and `frontend-admin/src/types/user.ts` as the primary admin user-management contract.
- Existing `api/userManagement.ts` and `types/userManagement.ts` may remain temporarily, but they should not remain the main execution path for the active user-management flow.

## Scope

### In Scope

- align the active admin user-management flow to one primary API/type contract
- fix `stores/userManagement.ts`
- fix `views/admin/UserManagementList.vue`
- fix `components/UserDetailDrawer.vue`
- fix `components/BatchStatusDialog.vue`
- clear user-management-specific type drift that currently breaks `frontend-admin` type-check
- sync OpenSpec and tracking docs after implementation

### Out Of Scope

- adding new user-management pages
- changing backend user-management APIs
- broad admin-wide type refactors
- fixing unrelated announcement/statistics/price-config TypeScript failures
- redesigning the user-management UI

## Current State Summary

### Contract Drift

The current admin user-management area contains two overlapping API/type tracks:

1. `api/user.ts` with `types/user.ts`
2. `api/userManagement.ts` with `types/userManagement.ts`

The active store and components are effectively mixing these two tracks.
That causes parameter-shape mismatches, response-shape mismatches, and child-table type mismatches inside the user detail drawer.

### Error Pattern

The current failures are not feature gaps.
They are contract consistency failures.
Typical symptoms include:

- dialog submits one payload shape while the store expects another
- store returns one response model while components expect another
- detail drawer child tables expect older lightweight item types while store calls return richer or differently-shaped models

## Recommended Approach

### Approach 1: Keep One Primary Contract, Recommended

Use `api/user.ts` and `types/user.ts` as the primary admin user-management contract.

Then:

- align `stores/userManagement.ts` to that contract
- update the active user-management UI chain to consume that contract consistently
- keep the older `userManagement` API/type files only as non-primary compatibility leftovers for now

Why this is recommended:

- it matches the richer current store/use-case surface
- it minimizes scope while still restoring coherence
- it leaves open a later cleanup change to fully remove duplication

### Approach 2: Promote `userManagement` Files Instead

This would require bending the current store and active pages toward the thinner `userManagement` contract.

Why it is not recommended:

- it creates more adaptation work in this round
- it works against the more complete current store and page behavior

### Approach 3: Only Patch the Type Errors

Fix each type-check error locally without explicitly choosing one contract.

Why it is not recommended:

- it would hide the problem rather than resolve it
- it would leave the active user-management path structurally inconsistent

## Design Decisions

### Decision 1: `api/user.ts` And `types/user.ts` Become The Primary Contract

The active admin user-management flow should converge on the richer `user.ts` model set.
This includes:

- list responses
- user detail responses
- batch status payloads
- child table records returned from user detail-related endpoints

### Decision 2: Store First, Components Second

Implementation should proceed in dependency order:

1. correct primary types and API signatures if needed
2. align `stores/userManagement.ts`
3. align `UserManagementList.vue`
4. align `UserDetailDrawer.vue`
5. align `BatchStatusDialog.vue`

This avoids chasing component errors while the underlying contract is still unstable.

### Decision 3: Compatibility Files May Remain Temporarily

This round does not need to fully delete `api/userManagement.ts` and `types/userManagement.ts`.
However, they should stop driving the active execution path for the main user-management flow.

### Decision 4: No Backend Expansion

The backend is not the problem in this round.
If a mismatch appears, the frontend contract should be corrected to the current backend rather than adding backend endpoints.

## File Boundaries

### Expected Primary Touch Points

- `frontend-admin/src/api/user.ts`
- `frontend-admin/src/types/user.ts`
- `frontend-admin/src/stores/userManagement.ts`
- `frontend-admin/src/views/admin/UserManagementList.vue`
- `frontend-admin/src/components/UserDetailDrawer.vue`
- `frontend-admin/src/components/BatchStatusDialog.vue`

### Secondary Touch Points

Only if needed for compatibility clarification:

- `frontend-admin/src/api/userManagement.ts`
- `frontend-admin/src/types/userManagement.ts`

### Files To Avoid Touching

- backend user-management controllers/services
- unrelated admin modules
- new route/page creation outside the current user-management flow

## Risks

### Risk 1: Detail Drawer Contains Multiple Child Data Models

`UserDetailDrawer.vue` mixes several sub-tables.
If contract alignment is done carelessly, one child-table fix may break another.

Mitigation:

- align one response path at a time
- use explicit mapping at the component boundary only when necessary

### Risk 2: Batch Status Action May Be Semantically Drifted

The current dialog error suggests that the request payload shape already drifted.

Mitigation:

- align dialog submission strictly to the store method contract
- avoid hidden implicit conversions in the component

### Risk 3: Duplicated Types Can Reintroduce Drift

Even after the active path is fixed, the duplicate files may confuse future work.

Mitigation:

- clearly treat one pair as primary in this round
- consider full duplication cleanup only in a later dedicated change

## Validation Focus

### Functional Validation

- user list loads correctly
- single-user status update still works
- batch status dialog submits the expected payload
- user detail drawer opens and loads correctly
- charging-record tab in the drawer renders correctly
- reservation tab in the drawer renders correctly
- violation tab in the drawer renders correctly

### Type Validation

The target of this round is to eliminate the current user-management contract drift errors from `frontend-admin` type-check.
Unrelated module failures may remain and must be recorded as pre-existing.

## Acceptance Standard

This change is complete when:

- the active admin user-management flow uses one primary API/type contract
- `stores/userManagement.ts` is consistent with that contract
- `UserManagementList.vue`, `UserDetailDrawer.vue`, and `BatchStatusDialog.vue` no longer conflict with the active store/API models
- no new user-management features were added beyond contract repair
- documentation reflects the repaired state

## Recommended Next Step After Approval

1. write a focused OpenSpec change for `user-management-contract-alignment`
2. create the implementation plan
3. align the store and active components in dependency order
4. run targeted type-check validation
5. sync module tracking docs