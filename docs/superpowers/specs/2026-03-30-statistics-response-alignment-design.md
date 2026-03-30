# Statistics Response Alignment Design

## Date

2026-03-30

## Goal

This design defines one narrow alignment change for the `frontend-admin` statistics module.

The purpose is not to add new statistics features.
The purpose is to repair the statistics API and store contract so the admin dashboard consumes direct overview data and exports binary files correctly.

## Scope

### In Scope

- align `frontend-admin/src/api/statisticsApi.ts`
- align `frontend-admin/src/stores/statistics.ts`
- keep `frontend-admin/src/views/admin/StatisticsDashboard.vue` behavior unchanged unless a small type follow-up is required
- remove current statistics-specific `frontend-admin` type-check failures
- sync OpenSpec and tracking docs after implementation

### Out Of Scope

- charging pile icon fixes
- backend statistics changes
- chart feature expansion
- broad request-wrapper refactors across all modules

## Problem Summary

The statistics module currently has two different response problems:

1. overview requests are statically typed like raw `AxiosResponse` even though the shared request wrapper already returns unwrapped data
2. export requests use the shared request wrapper even though the backend returns raw binary bytes instead of the wrapped `Result<T>` envelope

That causes the current type-check failures and also makes the export path structurally unsafe at runtime.

## Recommended Approach

### Approach 1: Split Wrapped And Binary Calls At The API Boundary, Recommended

- keep overview-style statistics calls on the shared request wrapper, but type them as direct data models
- move export to a dedicated raw axios request with auth header passthrough and `responseType: 'blob'`
- keep the store thin and direct

Why this is recommended:

- it matches the actual backend behavior
- it fixes both typing and runtime correctness
- it stays within one small module

### Approach 2: Only Add Type Assertions In The Store

Why it is not recommended:

- it would not fix the export path semantics
- it hides the real contract mismatch

### Approach 3: Refactor The Shared Request Wrapper For Binary Awareness

Why it is not recommended:

- it expands this change far beyond the statistics slice
- it introduces unnecessary cross-module risk

## Validation Focus

- statistics overview no longer reports `AxiosResponse` shape errors
- statistics export no longer requires unsafe blob casting
- `frontend-admin` type-check no longer reports statistics-specific failures