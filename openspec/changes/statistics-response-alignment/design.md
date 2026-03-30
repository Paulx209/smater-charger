# Design: statistics-response-alignment

## Problem

The statistics overview path should consume direct data models from the shared request wrapper, while the export path should consume raw blob data.
The current API layer does not describe that difference correctly.

## Decision

- keep overview requests on the shared request wrapper with direct model types
- move export to a raw blob request path that bypasses the wrapped-response assumption
- keep the store simple and dashboard-facing behavior unchanged

## Validation

- statistics overview assigns directly to `StatisticsOverview`
- statistics export returns `Blob`
- `frontend-admin` type-check no longer reports statistics-specific failures