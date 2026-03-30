# Statistics Response Alignment Implementation Plan

## Scope

This plan covers only the `statistics-response-alignment` change.

Included:

- repair `frontend-admin/src/api/statisticsApi.ts`
- repair `frontend-admin/src/stores/statistics.ts`
- keep statistics dashboard behavior stable
- update OpenSpec and tracking docs after implementation

Excluded:

- charging pile icon fixes
- backend changes
- broader request-wrapper refactors

## Objective

Restore one coherent statistics contract in `frontend-admin` so overview requests return direct data models and export requests download real blob data safely.

## Deliverables

1. Corrected statistics API typing for overview requests
2. Corrected binary export request implementation
3. Corrected statistics store usage
4. New OpenSpec change directory for `statistics-response-alignment`
5. Updated baseline and inventory docs

## Execution Strategy

### Phase 1: OpenSpec Setup

- create the change directory
- add proposal, design, tasks, and focused spec

### Phase 2: API Contract Alignment

- type wrapped statistics requests as direct models
- split export into a raw blob request path

### Phase 3: Store Alignment

- remove unsafe blob casting
- keep dashboard-facing API stable

### Phase 4: Validation And Docs

- run `npm.cmd run type-check` in `frontend-admin`
- confirm statistics-specific errors are removed
- record remaining non-statistics errors separately
- update OpenSpec tasks and project docs

## Exit Condition

This plan is complete when statistics-specific `frontend-admin` type-check failures are cleared and the export path is aligned to the backend binary response.