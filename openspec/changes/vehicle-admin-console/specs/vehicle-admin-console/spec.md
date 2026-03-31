# Spec: vehicle-admin-console

## ADDED Requirements

### Requirement: Admin vehicle list

The system SHALL provide an admin vehicle list endpoint and page that support pagination and filtering by user ID and license plate.

#### Scenario: Admin queries vehicles

- **WHEN** an administrator opens the vehicle console
- **THEN** the system returns a paged vehicle list
- **AND** the admin can filter by `userId` and `licensePlate`

### Requirement: Admin vehicle detail

The system SHALL provide an admin vehicle detail endpoint and detail page.

#### Scenario: Admin views vehicle detail

- **WHEN** an administrator opens a vehicle detail page
- **THEN** the system returns vehicle information, owner information, and timestamps

### Requirement: Admin vehicle delete

The system SHALL allow administrators to delete a vehicle record.

#### Scenario: Admin deletes default vehicle

- **WHEN** an administrator deletes a default vehicle
- **THEN** the system deletes the target vehicle
- **AND** reassigns another remaining vehicle of the same user as default when one exists
