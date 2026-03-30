# Tasks: price-config-boundary-alignment

## 1. OpenSpec

- [x] 1.1 Create the `price-config-boundary-alignment` change directory
- [x] 1.2 Add proposal, design, tasks, and focused requirement spec

## 2. Backend boundary split

- [ ] 2.1 Move admin price-config management endpoints to `/admin/price-config`
- [ ] 2.2 Keep client read-only endpoints under `/price-config/current` and `/price-config/estimate`

## 3. Admin and client alignment

- [ ] 3.1 Align `frontend-admin` price-config API/store to the admin route group
- [ ] 3.2 Remove client admin price-config routes and pages
- [ ] 3.3 Shrink `frontend-client` price-config API/store to read-only behavior

## 4. Validation and docs

- [ ] 4.1 Run targeted backend and frontend validation for the price-config boundary
- [ ] 4.2 Update module baseline and project inventory documents
- [ ] 4.3 Record any remaining non-price-config validation failures separately