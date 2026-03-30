# Design: user-management-contract-alignment

## Problem

The active admin user-management chain is structurally inconsistent.
The store already depends on the richer `user.ts` contract, while the list page, detail drawer, password dialog, and batch dialog still assume the older `userManagement.ts` contract.

That mismatch currently surfaces as:

- `enabled` payload fields sent to APIs that expect `status`
- components expecting `email`, `realName`, and `enabled` while the backend returns `phone`, `nickname`, `name`, and `status`
- child tables expecting legacy `pileName`, `description`, and `penaltyAmount` shapes instead of the real backend response models

## Decision

### Decision 1: `api/user.ts` And `types/user.ts` Remain The Primary Contract

The active user-management flow will converge on the current backend-facing API and type pair already used by the store.
The fix is to update the active consumers, not to promote the legacy compatibility files.

### Decision 2: Align To Backend Fields Instead Of Invented UI Fields

The active views will render the fields the backend actually returns.
Examples:

- `status` instead of `enabled`
- `name` instead of `realName`
- no direct `email` field in the user detail model
- charging, reservation, and violation tables use the real item shapes

### Decision 3: Store First, Components Second

The repair order is:

1. normalize primary types if needed
2. normalize the store method signatures
3. align list page interactions
4. align detail drawer tables
5. align batch and password dialogs

This keeps the component fixes narrow and predictable.

## Target State

After this change:

- the user list page toggles status with `{ status }`
- the batch status dialog submits `{ userIds, status }`
- the password reset dialog consumes the same user model as the page
- the detail drawer tabs render the real charging, reservation, and violation response items
- user-management-specific type-check failures are removed

## Validation

- `BatchStatusDialog.vue` no longer submits `enabled`
- `UserManagementList.vue` no longer passes a bare boolean to `updateStatus`
- `UserDetailDrawer.vue` no longer depends on legacy child-table types
- `frontend-admin` type-check no longer reports the current user-management drift errors