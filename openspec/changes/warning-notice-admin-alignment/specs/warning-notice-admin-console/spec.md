# Spec: warning-notice-admin-alignment

## ADDED Requirements

### Requirement: Admin warning-notice list access

The system SHALL provide a dedicated admin warning-notice list view backed by admin warning-notice APIs.

#### Scenario: open admin warning-notice list
- **WHEN** an authenticated admin visits the warning-notice management page
- **THEN** the frontend calls `GET /admin/warning-notices`
- **AND** the page renders a paginated warning-notice list

#### Scenario: filter admin warning-notice list
- **WHEN** an authenticated admin applies supported filters
- **THEN** the frontend passes the selected query parameters to `GET /admin/warning-notices`
- **AND** the returned list reflects the selected filters

### Requirement: Admin warning-notice detail access

The system SHALL provide a dedicated admin warning-notice detail view for a single warning notice.

#### Scenario: open admin warning-notice detail
- **WHEN** an authenticated admin opens a warning-notice detail page by notice ID
- **THEN** the frontend calls `GET /admin/warning-notices/{id}`
- **AND** the page renders the returned warning-notice detail fields

### Requirement: Admin warning-notice bulk operations

The system SHALL allow admins to batch mark warning notices as read and delete notices from the admin console.

#### Scenario: batch mark notices as read
- **WHEN** an authenticated admin selects unread notices and confirms a bulk read action
- **THEN** the frontend calls `PUT /admin/warning-notices/read`
- **AND** the selected notices are persisted as read

#### Scenario: delete a warning notice
- **WHEN** an authenticated admin deletes a warning notice from the console
- **THEN** the frontend calls `DELETE /admin/warning-notices/{id}`
- **AND** the notice is removed from the admin list

### Requirement: Admin global threshold configuration

The system SHALL let admins read and update the global warning threshold without overwriting user-specific threshold settings.

#### Scenario: read global threshold
- **WHEN** an authenticated admin opens the warning-notice settings page
- **THEN** the frontend calls `GET /admin/warning-notices/config/threshold`
- **AND** the returned value reflects the system-level threshold

#### Scenario: update global threshold
- **WHEN** an authenticated admin submits a valid threshold value
- **THEN** the frontend calls `PUT /admin/warning-notices/config/threshold`
- **AND** the system-level threshold is updated
- **AND** user-specific threshold records remain unchanged