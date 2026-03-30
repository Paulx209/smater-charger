# Announcement Store Response Alignment Implementation Plan

## Scope

This plan covers only the `announcement-store-response-alignment` change.

Included:

- repair `frontend-admin/src/api/announcement.ts`
- repair `frontend-admin/src/stores/announcement.ts`
- repair `frontend-admin/src/views/admin/AnnouncementForm.vue`
- repair `frontend-admin/src/views/admin/AnnouncementList.vue`
- update OpenSpec and tracking docs after implementation

Excluded:

- frontend-client announcement changes
- backend changes
- statistics fixes
- charging pile icon fixes

## Objective

Restore a single coherent announcement contract in `frontend-admin` so the admin announcement store and pages consume direct data models instead of inferred `AxiosResponse` wrappers.

## Deliverables

1. Corrected admin announcement API typings
2. Corrected admin announcement store state assignments
3. Corrected admin announcement form detail loading
4. Corrected admin announcement list status rendering and pagination use
5. New OpenSpec change directory for `announcement-store-response-alignment`
6. Updated baseline and inventory docs

## Execution Strategy

### Phase 1: OpenSpec Setup

- create the change directory
- add proposal, design, tasks, and focused spec

### Phase 2: API Contract Alignment

- update `api/announcement.ts` to return direct data models
- keep runtime behavior unchanged

### Phase 3: Store And View Alignment

- update `stores/announcement.ts` to consume corrected API types
- update `AnnouncementForm.vue`
- update `AnnouncementList.vue`

### Phase 4: Validation And Docs

- run `npm.cmd run type-check` in `frontend-admin`
- confirm announcement-specific errors are removed
- record remaining non-announcement errors separately
- update OpenSpec tasks and project docs

## Exit Condition

This plan is complete when announcement-specific `frontend-admin` type-check failures are cleared and project tracking docs reflect the repaired admin announcement slice.