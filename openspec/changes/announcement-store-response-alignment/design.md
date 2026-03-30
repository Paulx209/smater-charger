# Design: announcement-store-response-alignment

## Problem

The announcement request wrapper returns direct data at runtime, but the static typing of `api/announcement.ts` still leaks raw response semantics into the admin store and views.
That mismatch causes the current announcement-specific type-check failures.

## Decision

- fix the contract at `frontend-admin/src/api/announcement.ts`
- keep `stores/announcement.ts` thin and direct
- align `AnnouncementForm.vue` and `AnnouncementList.vue` to the corrected models

## Validation

- announcement store no longer reports `AxiosResponse`-shape errors
- announcement form loads detail fields directly
- announcement list renders typed status values without implicit `any`