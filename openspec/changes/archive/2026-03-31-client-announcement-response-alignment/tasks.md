# Tasks: client-announcement-response-alignment

## 1. OpenSpec

- [x] 1.1 Create the `client-announcement-response-alignment` change directory
- [x] 1.2 Add proposal, design, tasks, and focused requirement spec

## 2. Client announcement alignment

- [x] 2.1 Align announcement API return types with unwrapped request responses
- [x] 2.2 Fix announcement store response usage
- [x] 2.3 Fix announcement carousel response usage
- [x] 2.4 Fix the deterministic null access in `warningNotice` store

## 3. Validation and docs

- [x] 3.1 Run `frontend-client` type-check and confirm announcement-related errors are cleared
- [x] 3.2 Update baseline, handoff, and project inventory docs
- [x] 3.3 Record remaining unrelated `frontend-client` errors separately

## Notes

Remaining unrelated `frontend-client` type-check errors are now concentrated in:

- `src/views/ChargingRecordList.vue`
- `src/views/ChargingRecordStatistics.vue`
- `src/views/ReservationCreate.vue`
- `src/views/ReservationDetail.vue`
- `src/components/__tests__/HelloWorld.spec.ts`