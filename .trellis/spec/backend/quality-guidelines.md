# Backend Quality Guidelines

The backend is Java 17 with Spring Boot 3.2.2 and Maven.

## Build and Test Commands

Run backend verification from `backend/`:

```bash
mvn test
mvn spring-boot:run
```

There is no populated `src/test` tree at bootstrap time, even though
`spring-boot-starter-test` is configured. Treat missing tests as an existing
project gap, not as a reason to skip compiling or targeted manual checks.

## Code Style

- Follow the existing layered architecture. Controllers delegate; services own
  business logic and transactions; repositories own persistence.
- Prefer constructor injection. Lombok `@RequiredArgsConstructor` is used in
  several controllers; some service implementations use explicit constructors.
  Either is acceptable if dependencies remain `final`.
- Use Lombok for simple data carriers where the project already does:
  `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, and
  `@EqualsAndHashCode(callSuper = true)`.
- Keep response mapping explicit in services. The codebase uses helper methods
  such as `buildReservationResponse(...)` rather than exposing entities.
- Use Java streams for DTO mapping and id collection where it improves
  readability; keep complex business branching plain and explicit.

## API Conventions

- Backend context path is `/api`; controller `@RequestMapping` values are
  relative to that.
- Public/client controllers use resource paths such as `/reservations` and
  `/charging-piles`.
- Admin controllers use admin-oriented names or prefixes, such as
  `AdminReservationController`, `AdminPriceConfigController`, and
  `/admin/users`.
- Page responses are commonly converted to maps or page DTOs containing
  records/content plus total/page metadata.

## Dependencies

Current major dependencies:

- Spring Boot starters: web, data-jpa, validation, aop, security, redis, test
- MySQL Connector/J
- Redisson for distributed locks
- JJWT 0.12.3 for JWT
- Hutool and Apache POI for utilities/import-export

Do not add a dependency when Spring/JDK/project utilities already cover the
need.

## Review Checklist

- Does the change keep business logic out of controllers?
- Are all expected domain failures represented by `BusinessException` and
  `ResultCode`?
- Are write methods transactional and read methods marked read-only where
  appropriate?
- Are list responses avoiding obvious N+1 repository access?
- Are DTOs updated on both request/response sides when API shape changes?
- Are logs useful and free of secrets?
- If a database shape changed, are entity, schema, and migration notes aligned?

## Scenario: Reservation Time-Slot Availability

### 1. Scope / Trigger

- Trigger: Reservation changes that affect `ReservationController`,
  `ReservationCreateRequest`, `AvailabilityCheckResponse`,
  `ReservationService`, or the owner reservation UI.
- Reservations are time-slot occupancy. They must not make the whole
  `ChargingPile` permanently unavailable for future reservations.

### 2. Signatures

- `POST /api/reservations`
  - Request: `chargingPileId`, optional `startTime`, optional `endTime`
  - Response: `ReservationResponse`
- `GET /api/reservations/check-availability`
  - Query: `pileId`, `startTime`, `endTime`
  - Response: `AvailabilityCheckResponse`
- `GET /api/reservations/piles/{pileId}/reserved-slots`
  - Response: list of reservation slot objects

### 3. Contracts

- `ReservationCreateRequest.endTime` is optional for backward compatibility.
  When absent, the service defaults to `startTime.plusHours(2)`.
- Availability checks and creation must test real overlap:
  `candidateStart < existingEnd && existingStart < candidateEnd`.
- `ChargingPile.status` blocks reservation unless it is `IDLE`.
  `RESERVED` is not a runtime pile status; reservations are represented only
  by `reservation` rows.
- Creating a reservation must not set `ChargingPile.status` to `RESERVED`.

### 4. Validation & Error Matrix

- Missing pile -> `CHARGING_PILE_NOT_FOUND`.
- Non-reservable pile status -> `CHARGING_PILE_NOT_IDLE`.
- `endTime <= startTime` -> `INVALID_TIME_RANGE`.
- Duration greater than four hours -> `INVALID_TIME_RANGE`.
- Overlapping pending reservation -> `TIME_CONFLICT`.

### 5. Good/Base/Bad Cases

- Good: existing reservation 10:00-12:00, new request 13:00-14:00 succeeds.
- Base: existing reservation 10:00-12:00, new request 11:00-13:00 fails.
- Bad: blocking all new reservations because a pile has any pending
  reservation, regardless of the requested time.

### 6. Tests Required

- Service test for non-overlapping reservations on the same pile.
- Service test for overlapping reservations returning `TIME_CONFLICT`.
- API/frontend contract test or type-check proving `endTime` and
  `reservedTimeSlots` are represented on both sides.

### 7. Wrong vs Correct

#### Wrong

```java
if (!pendingReservations.isEmpty()) {
    throw new BusinessException(ResultCode.TIME_CONFLICT);
}
chargingPile.setStatus(ChargingPileStatus.RESERVED);
```

#### Correct

```java
boolean hasConflict = pendingReservations.stream()
        .anyMatch(r -> start.isBefore(r.getEndTime()) && r.getStartTime().isBefore(end));
if (hasConflict) {
    throw new BusinessException(ResultCode.TIME_CONFLICT);
}
```

## Scenario: Charging State Lifecycle

### 1. Scope / Trigger

- Trigger: Changes that affect `ChargingRecordController`,
  `ChargingRecordStartRequest`, `ChargingRecordService`,
  `StartChargingTxService`, `OvertimeWarningTask`, `WarningNoticeService`, or
  `ChargingPileStatus`.
- `ChargingPile.status` is a current runtime display/availability state, not
  the source of truth for reservations or historical charging facts.

### 2. Signatures

- `POST /api/charging-record/start`
  - Request: `chargingPileId`, optional `vehicleId`, required `targetType`,
    required `targetValue`
  - `targetType`: `DURATION` for minutes, `KWH` for kWh
  - Response: `ChargingRecordResponse`
- `POST /api/charging-record/end/{id}`
  - Manual stop by owner
  - Response: `ChargingRecordResponse`
- `POST /api/charging-record/{id}/leave`
  - Owner confirms the vehicle has left
  - Response: `ChargingRecordResponse`
- DB fields on `charging_record`: `leave_time`, `target_type`,
  `target_value`, `target_duration_minutes`, `target_kwh`,
  `target_end_time`, `end_reason`, `pre_end_notice_sent`.

### 3. Contracts

- Valid runtime pile statuses are `IDLE`, `CHARGING`, `WAITING_LEAVE`,
  `OVERTIME`, and `FAULT`.
- `RESERVED` must not be used as a runtime pile status. A reservation is a
  time-slot row in `reservation`, and creating/cancelling/expiring it must not
  update `charging_pile.status`.
- Starting charging requires an `IDLE` pile and sets the pile to `CHARGING`.
- Starting charging computes both target forms:
  - `DURATION`: `targetKwh = pile.power * targetDurationMinutes / 60`
  - `KWH`: `targetDurationMinutes = ceil(targetKwh * 60 / pile.power)`
- Manual stop and automatic target-time stop must call the same completion
  logic: calculate actual duration/kWh/fee, set record `COMPLETED`, set pile
  `WAITING_LEAVE`, set `endReason`, and send a completion notice.
- Owner leave confirmation sets `leaveTime` and releases only
  `WAITING_LEAVE` or `OVERTIME` piles to `IDLE`.
- Overtime scanning runs every five minutes, finds `COMPLETED` records with
  `leaveTime == null`, sets/keeps the pile `OVERTIME`, and sends a notice on
  every run without deduplicating by record/type.

### 4. Validation & Error Matrix

- Missing pile -> `CHARGING_PILE_NOT_FOUND`.
- Pile status not `IDLE` on start -> `CHARGING_PILE_NOT_IDLE`.
- User already has active charging -> `USER_ALREADY_CHARGING`.
- Another user's active reservation covers now -> `NO_VALID_RESERVATION`.
- Missing or non-positive target -> `BAD_REQUEST`.
- Invalid pile power for target calculation -> `BAD_REQUEST`.
- Manual stop for non-owner -> `FORBIDDEN`.
- Manual stop for non-`CHARGING` record -> `CHARGING_RECORD_NOT_CHARGING`.
- Leave confirmation for non-owner -> `FORBIDDEN`.
- Leave confirmation before record completion -> `BAD_REQUEST`.

### 5. Good/Base/Bad Cases

- Good: user starts with `targetType=KWH`, target duration is calculated,
  auto-completion later sets the pile to `WAITING_LEAVE`.
- Base: user manually stops early; fee is based on actual elapsed time, not the
  original target.
- Bad: completion sets the pile directly to `IDLE`, letting another user use a
  spot before the owner confirms leaving.
- Bad: overtime notices are skipped because a previous
  `OVERTIME_WARNING` exists for the same record.

### 6. Tests Required

- Unit test target conversion for `DURATION` and `KWH`.
- Unit or service test manual completion and auto completion share the same
  state/fee outcomes.
- Unit test owner leave confirmation releases only the current completed
  not-left record.
- Unit test overtime scanning sends a notice on every run.
- Migration/schema check that `schema.sql`, entity fields, and migration
  columns are aligned.

### 7. Wrong vs Correct

#### Wrong

```java
chargingPile.setStatus(ChargingPileStatus.IDLE);
chargingRecord.setStatus(ChargingRecordStatus.OVERTIME);
```

#### Correct

```java
chargingRecord.setStatus(ChargingRecordStatus.COMPLETED);
chargingPile.setStatus(ChargingPileStatus.WAITING_LEAVE);
// The overtime task later marks the pile OVERTIME until leaveTime is set.
```
