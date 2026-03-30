# Spec: charging-record-admin-console-and-hardening

## ADDED Requirements

### Requirement: Admin charging-record list access

The system SHALL provide a dedicated admin charging-record list view backed by the existing admin list API.

#### Scenario: open admin charging-record list
- **WHEN** an authenticated admin visits the charging-record management page
- **THEN** the frontend calls `GET /charging-record/admin/all`
- **AND** the page renders a paginated charging-record list

#### Scenario: filter admin charging-record list
- **WHEN** an authenticated admin applies supported filters
- **THEN** the frontend passes the selected query parameters to `GET /charging-record/admin/all`
- **AND** the returned list reflects the selected filters

---

### Requirement: Admin charging-record detail access

The system SHALL provide a dedicated admin charging-record detail view for a single record.

#### Scenario: open admin charging-record detail
- **WHEN** an authenticated admin opens a charging-record detail page by record ID
- **THEN** the frontend calls `GET /charging-record/admin/{id}`
- **AND** the page renders the returned charging-record detail fields

---

### Requirement: Charging-record docs reflect code reality

The project SHALL track charging-record work based on the current codebase rather than stale plans.

#### Scenario: backend hardening already exists in code
- **WHEN** the charging-record change is documented after implementation
- **THEN** the updated tasks and baseline docs describe existing backend hardening as landed work
- **AND** the admin console is documented as the primary gap closed by this change