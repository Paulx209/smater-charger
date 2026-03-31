# Requirement: Client Announcement Response Alignment

## ADDED Requirements

### Requirement: Announcement API responses must use unwrapped business data
The client announcement module MUST treat API calls as returning unwrapped business payloads rather than `AxiosResponse` objects.

#### Scenario: list request returns a page payload
- **WHEN** the client calls the announcement list API
- **THEN** the return type is the page payload object itself
- **AND** the store can read `content`, `totalElements`, `number`, and `size` directly

#### Scenario: detail request returns one announcement
- **WHEN** the client calls the announcement detail API
- **THEN** the return type is one `AnnouncementClientInfo`
- **AND** the store can assign it directly to the current announcement state

#### Scenario: latest request returns a list payload
- **WHEN** the client calls the latest announcements API
- **THEN** the return type is an array of `AnnouncementClientInfo`
- **AND** the carousel can assign it directly to its local state

### Requirement: warning notice store must avoid unsafe indexed access
The client warning notice store MUST avoid assuming that an indexed array lookup always returns a defined item.

#### Scenario: removing a warning notice from the list
- **WHEN** the store deletes a warning notice by id
- **THEN** it checks the indexed item safely before reading `isRead`
- **AND** strict TypeScript mode does not report a possible `undefined` access