# Tasks: user-management-contract-alignment

## 1. OpenSpec

- [x] 1.1 Create the `user-management-contract-alignment` change directory
- [x] 1.2 Add proposal, design, tasks, and focused requirement spec

## 2. Primary contract alignment

- [x] 2.1 Align `frontend-admin/src/types/user.ts` to the backend user-management fields used by the active UI
- [x] 2.2 Keep `frontend-admin/src/api/user.ts` as the primary API entry for the active user-management flow

## 3. Store and component repair

- [x] 3.1 Align `frontend-admin/src/stores/userManagement.ts` to the primary contract
- [x] 3.2 Update `frontend-admin/src/views/admin/UserManagementList.vue` to use the real user fields and status payload
- [x] 3.3 Update `frontend-admin/src/components/UserDetailDrawer.vue` to use the real child-table models
- [x] 3.4 Update `frontend-admin/src/components/BatchStatusDialog.vue` to submit the real batch payload
- [x] 3.5 Update `frontend-admin/src/components/PasswordResetDialog.vue` to consume the real user model

## 4. Validation and docs

- [x] 4.1 Run targeted validation for the user-management slice in `frontend-admin`
- [x] 4.2 Update module baseline and project inventory documents
- [x] 4.3 Record any remaining non-user-management validation failures separately