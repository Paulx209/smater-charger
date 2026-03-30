# Spec: user-management-contract-alignment

## ADDED Requirements

### Requirement: Admin user-management active flow uses one primary frontend contract

The system SHALL implement the active admin user-management flow against a single primary frontend contract.

#### Scenario: active list page uses primary contract
- **WHEN** the admin user-management page loads or filters users
- **THEN** the page consumes the primary user-management types from `types/user.ts`
- **AND** the list interaction path uses `api/user.ts`

#### Scenario: single-user status update submits the real payload
- **WHEN** an admin toggles a user's status from the list page
- **THEN** the frontend sends `PUT /admin/users/{id}/status`
- **AND** the request body includes `status`
- **AND** the frontend does not send a bare boolean or `enabled` payload

### Requirement: Batch user status update matches backend payload

The system SHALL submit batch status changes using the backend request model.

#### Scenario: batch status dialog confirms a change
- **WHEN** an admin confirms a batch enable or disable action
- **THEN** the frontend sends `PUT /admin/users/batch-status`
- **AND** the request body includes `userIds` and `status`

### Requirement: User detail drawer renders real child-table models

The system SHALL render user-related record tabs using the current backend response shapes.

#### Scenario: load charging records in the drawer
- **WHEN** an admin opens the charging-record tab for a user
- **THEN** the frontend consumes the current charging-record response model
- **AND** the UI no longer requires legacy `ChargingRecordInfo` fields that are not part of the active contract

#### Scenario: load reservations and violations in the drawer
- **WHEN** an admin opens the reservation or violation tabs for a user
- **THEN** the frontend consumes the current reservation and violation response models
- **AND** the UI no longer requires legacy placeholder fields that are not part of the active contract