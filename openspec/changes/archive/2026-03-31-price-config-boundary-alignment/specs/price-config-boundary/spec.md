# Spec: price-config-boundary-alignment

## ADDED Requirements

### Requirement: Admin price-config management is isolated to admin endpoints

The system SHALL expose price-config management capabilities through admin-specific endpoints.

#### Scenario: admin manages price configs
- **WHEN** the admin app lists, creates, edits, deletes, or toggles price configs
- **THEN** it uses `/admin/price-config`
- **AND** client-facing routes are not used for management behavior

### Requirement: Client price-config access is read-only

The system SHALL limit client price-config behavior to effective-price lookup and fee estimation.

#### Scenario: client reads current price config
- **WHEN** a client page needs the current effective price config for a charging-pile type
- **THEN** it calls the read-only client endpoint
- **AND** no admin management route or page is required

#### Scenario: client estimates price
- **WHEN** a client page estimates a charging fee
- **THEN** it calls the read-only estimate endpoint
- **AND** no admin management API is exposed through the client store