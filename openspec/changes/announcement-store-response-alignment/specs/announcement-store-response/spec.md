# Spec: announcement-store-response-alignment

## ADDED Requirements

### Requirement: Admin announcement API returns direct data models to callers

The system SHALL type admin announcement API calls according to the unwrapped data returned by the shared request wrapper.

#### Scenario: fetch admin announcement list
- **WHEN** the admin announcement list requests data
- **THEN** the API layer returns a typed announcement list model
- **AND** downstream callers can read `content`, `totalElements`, `number`, and `size` directly

#### Scenario: fetch admin announcement detail
- **WHEN** the admin announcement form loads an existing announcement
- **THEN** the API layer returns a typed announcement detail model
- **AND** downstream callers can read `title`, `content`, `startTime`, and `endTime` directly

### Requirement: Admin announcement pages consume typed status values

The system SHALL render announcement status text and color using typed enum-backed values.

#### Scenario: render admin announcement list rows
- **WHEN** the announcement list renders a row status
- **THEN** status text and color are indexed using typed announcement status values
- **AND** the page does not rely on implicit `any` indexing