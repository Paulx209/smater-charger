# Design: price-config-boundary-alignment

## Problem

The current price-config implementation mixes admin management and client-facing reads across both frontend and backend boundaries.
That makes the module easy to misuse and keeps the project state inconsistent with the intended application split.

## Decision

- move backend management endpoints to `/admin/price-config`
- keep client-facing reads under `/price-config/current` and `/price-config/estimate`
- keep `frontend-admin` as the only management surface
- remove client admin routes and pages
- shrink the client price-config API/store to read-only behavior

## Validation

- backend compiles after route split
- `frontend-admin` management flow still compiles and targets admin routes
- `frontend-client` no longer contains admin price-config routes or pages
- client price-info and price-estimate consumers still resolve the read-only API