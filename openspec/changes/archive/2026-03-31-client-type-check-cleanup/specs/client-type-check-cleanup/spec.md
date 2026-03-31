## ADDED Requirements

### Requirement: frontend-client type-check stability
The client application SHALL keep ue-tsc --build passing after module contract alignment work.

#### Scenario: charging record pages use stable status mappings
- **WHEN** charging record list and statistics pages read status-related text or color mappings
- **THEN** the type system SHALL accept the lookup without implicit ny errors

#### Scenario: reservation pages keep working with existing UI flow
- **WHEN** reservation create and reservation detail pages are type-checked
- **THEN** missing imports and local request typing mismatches SHALL NOT block compilation

#### Scenario: placeholder tests do not reference removed demo files
- **WHEN** the test directory is included in type-check
- **THEN** no test file SHALL import a component that does not exist in the project