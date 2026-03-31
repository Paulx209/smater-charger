# Tasks: warning-notice-admin-alignment

## 1. OpenSpec

- [x] 1.1 Create the `warning-notice-admin-alignment` change directory
- [x] 1.2 Add proposal, design, tasks, and focused requirement spec

## 2. Backend admin warning-notice APIs

- [x] 2.1 Add admin warning-notice list and detail endpoints
- [x] 2.2 Add admin batch-read and delete endpoints
- [x] 2.3 Add admin global threshold read and update endpoints
- [x] 2.4 Reuse the active warning-notice response model for admin views

## 3. frontend-admin warning-notice console

- [x] 3.1 Add warning-notice API, types, and store
- [x] 3.2 Add warning-notice list, detail, and settings views
- [x] 3.3 Add router and navigation entry for warning-notice management

## 4. Validation and docs

- [x] 4.1 Run targeted backend and `frontend-admin` validation
- [x] 4.2 Update module baseline, project inventory, and handoff docs
- [x] 4.3 Record any remaining unrelated validation failures separately

## Notes

- `backend`: `mvn.cmd -q -DskipTests compile` passed
- `frontend-admin`: `npm.cmd run type-check` passed
- Current unrelated engineering debt remains mainly in `frontend-client` historical TypeScript errors and is outside this change