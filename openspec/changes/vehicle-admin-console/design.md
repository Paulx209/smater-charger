# Design: vehicle-admin-console

## Backend

- Add `AdminVehicleController` under `/admin/vehicles`
- Extend `VehicleService` with admin list/detail/delete methods
- Reuse shared delete semantics so default vehicle reassignment stays consistent

## frontend-admin

- Add `vehicle.ts` API, types, and store
- Add `VehicleList.vue` and `VehicleDetail.vue`
- Add navigation and router entries

## Validation

- backend compile
- `frontend-admin` type-check
- manual review of list/detail/delete flow
