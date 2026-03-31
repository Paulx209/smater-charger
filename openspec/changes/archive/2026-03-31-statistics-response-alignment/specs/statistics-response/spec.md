# Spec: statistics-response-alignment

## ADDED Requirements

### Requirement: Admin statistics overview returns direct data models

The system SHALL type statistics overview requests according to the unwrapped data returned by the shared request wrapper.

#### Scenario: fetch statistics overview
- **WHEN** the admin statistics dashboard requests overview data
- **THEN** the API layer returns a typed `StatisticsOverview`
- **AND** the store can assign it directly without `AxiosResponse` handling

### Requirement: Admin statistics export uses binary response handling

The system SHALL request statistics export as raw binary data.

#### Scenario: export statistics workbook
- **WHEN** an admin exports statistics data
- **THEN** the frontend sends a request that expects a blob response
- **AND** the returned value can be downloaded without unsafe `AxiosResponse` casting