# Proposal: user-management-contract-alignment

## Context

The admin user-management module currently mixes two overlapping frontend contracts:

- `api/user.ts` with `types/user.ts`
- `api/userManagement.ts` with `types/userManagement.ts`

The active user-management page, drawer, and dialogs no longer agree on:

- top-level user fields
- status update payloads
- batch status payloads
- child-table item types for charging records, reservations, and violations

This is causing user-management-specific `frontend-admin` type-check failures and creating unnecessary confusion about which contract is authoritative.

## Change Summary

This change aligns the active admin user-management flow to one primary contract.

The implementation will:

- treat `frontend-admin/src/api/user.ts` as the active API source
- treat `frontend-admin/src/types/user.ts` as the active type source
- repair the active store and components to consume the same request and response shapes
- keep the older `userManagement` API/type files as temporary compatibility leftovers only

## Non-Goals

- adding new user-management pages
- changing backend user-management APIs
- broad admin-wide type cleanup
- fixing unrelated announcement, statistics, or charging-pile issues