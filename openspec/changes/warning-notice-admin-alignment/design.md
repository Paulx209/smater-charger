# Design: warning-notice-admin-alignment

## Problem

The warning-notice module lacks an admin-first workflow.
Existing support is limited to client usage and user-scoped actions.
That leaves the module partially implemented from the management perspective.

## Decision

- add admin warning-notice list, detail, batch-read, delete, and global-threshold endpoints
- add a dedicated warning-notice console in `frontend-admin`
- keep the client warning-notice flow unchanged
- keep user threshold handling separate from global threshold handling

## Validation

- backend compiles after admin warning-notice APIs are added
- `frontend-admin` type-check passes with the new warning-notice console
- admin can list, filter, inspect, batch-read, delete, and edit global threshold
- existing client warning-notice pages continue to work unchanged