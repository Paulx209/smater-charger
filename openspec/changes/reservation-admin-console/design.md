# Design: reservation-admin-console

## Problem

The reservation module lacks an admin-first workflow.
Existing reservation support is limited to client usage and user-detail side views.
That leaves the module partially implemented from the management perspective.

## Decision

- add admin reservation list, detail, and cancel endpoints
- add a dedicated reservation console in `frontend-admin`
- allow admin cancellation only for `PENDING` reservations
- keep the client reservation flow unchanged
- keep the user detail drawer reservation tab available

## Validation

- backend compiles after admin reservation APIs are added
- `frontend-admin` type-check passes with the new reservation console
- admin can list, filter, inspect, and cancel pending reservations
- existing client reservation pages continue to work unchanged