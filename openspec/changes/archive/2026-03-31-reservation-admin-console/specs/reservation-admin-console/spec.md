# Spec: reservation-admin-console

## ADDED Requirements

### Requirement: Admin reservation list access

The system SHALL provide a dedicated admin reservation list view backed by admin reservation APIs.

#### Scenario: open admin reservation list
- **WHEN** an authenticated admin visits the reservation management page
- **THEN** the frontend calls `GET /admin/reservations`
- **AND** the page renders a paginated reservation list

#### Scenario: filter admin reservation list
- **WHEN** an authenticated admin applies supported filters
- **THEN** the frontend passes the selected query parameters to `GET /admin/reservations`
- **AND** the returned list reflects the selected filters

### Requirement: Admin reservation detail access

The system SHALL provide a dedicated admin reservation detail view for a single reservation.

#### Scenario: open admin reservation detail
- **WHEN** an authenticated admin opens a reservation detail page by reservation ID
- **THEN** the frontend calls `GET /admin/reservations/{id}`
- **AND** the page renders the returned reservation detail fields

### Requirement: Admin reservation cancellation is limited to pending reservations

The system SHALL allow admins to cancel reservations only when the reservation is still pending.

#### Scenario: cancel a pending reservation
- **WHEN** an authenticated admin cancels a reservation whose status is `PENDING`
- **THEN** the backend updates the reservation status to `CANCELLED`
- **AND** the related charging pile is restored from `RESERVED` to `IDLE` when appropriate

#### Scenario: reject cancellation for a non-pending reservation
- **WHEN** an authenticated admin attempts to cancel a reservation whose status is not `PENDING`
- **THEN** the backend rejects the request with the existing reservation cancellation business error