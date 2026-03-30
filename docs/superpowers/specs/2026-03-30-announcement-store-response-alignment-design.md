# Announcement Store Response Alignment Design

## Date

2026-03-30

## Goal

This design defines one narrow alignment change for the `frontend-admin` announcement module.

The purpose is not to add new announcement features.
The purpose is to repair the active admin announcement chain so it consistently treats the API layer as returning already-unwrapped data.

## Scope

### In Scope

- align `frontend-admin/src/api/announcement.ts` response typing with the current request wrapper behavior
- align `frontend-admin/src/stores/announcement.ts` to the corrected API contract
- align `frontend-admin/src/views/admin/AnnouncementForm.vue`
- align `frontend-admin/src/views/admin/AnnouncementList.vue`
- remove current announcement-specific `frontend-admin` type-check failures
- sync OpenSpec and tracking docs after implementation

### Out Of Scope

- frontend-client announcement changes
- backend announcement API changes
- statistics module fixes
- charging pile icon fixes
- announcement UI redesign

## Problem Summary

The admin announcement module already uses a request wrapper that unwraps `response.data.data` in the response interceptor.
However, the announcement API typing still makes downstream code look like it receives `AxiosResponse` objects.

That drift causes:

- store assignments that expect `AnnouncementInfo` but receive a static type of `AxiosResponse<AnnouncementInfo>`
- list pagination code that cannot access `content`, `totalElements`, `number`, or `size`
- form detail loading that cannot safely read `title`, `content`, `startTime`, or `endTime`
- list status rendering with weakly typed enum indexing

## Recommended Approach

### Approach 1: Fix The API Contract At The Source, Recommended

Make `api/announcement.ts` explicitly return the already-unwrapped data types produced by the shared request wrapper.
Then keep store and view code simple and strongly typed.

Why this is recommended:

- it matches runtime behavior
- it removes the root cause instead of patching callers individually
- it stays within the small announcement slice

### Approach 2: Patch Store And Views With Type Assertions

Keep the current API signatures and coerce return values at each call site.

Why it is not recommended:

- it hides the real contract
- it duplicates correction logic across files

### Approach 3: Expand To Other Remaining Admin Type Errors

Fix announcement, statistics, and charging pile icon issues in one round.

Why it is not recommended:

- it breaks the small-granularity rule
- it mixes unrelated modules

## Design Decisions

### Decision 1: API Types Become The Contract Boundary

`frontend-admin/src/api/announcement.ts` should describe the values actually returned after interception, not raw Axios response objects.

### Decision 2: Store Stays Thin

`frontend-admin/src/stores/announcement.ts` should only manage state assignment, pagination, and user feedback.
It should not carry response-shape workaround logic.

### Decision 3: List And Form Consume Strong Types Directly

`AnnouncementList.vue` and `AnnouncementForm.vue` should assume direct `AnnouncementInfo` and paginated list models.
They should not need Axios-specific handling.

## Validation Focus

- admin announcement store no longer reports `AxiosResponse` shape errors
- admin announcement form can load detail fields directly
- admin announcement list can index status text and color without implicit `any`
- `frontend-admin` type-check no longer reports announcement-specific errors