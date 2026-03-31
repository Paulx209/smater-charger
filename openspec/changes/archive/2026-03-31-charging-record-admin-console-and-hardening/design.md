# Design: charging-record-admin-console-and-hardening

## Problem

The charging-record module has two different issues:

1. older planning artifacts still imply that backend hardening is mostly unfinished
2. the admin frontend still lacks a dedicated operational console for charging records

Code inspection shows that the backend already contains the main hardening measures from the old plan, so this round should not re-implement them blindly.
The primary product gap is the admin-side list and detail experience.

## Goals

1. align OpenSpec execution with the actual backend codebase
2. fill only the backend gaps still required by an admin charging-record console
3. add dedicated admin charging-record list and detail pages with filters and pagination

## Non-Goals

1. redesigning the end-user charging-record module
2. broad backend model refactors unrelated to the admin console
3. bundling payment, reservation, or realtime dashboard work into this change

## Decisions

### Decision 1: Code remains the source of truth

Older `openspec/changes/charging-record-frontend` files may be used for intent, but actual code decides what is already done versus still missing.

### Decision 2: Backend changes stay minimal

This change should preserve existing locking and batch-fetch logic.
Backend edits are limited to concrete gaps discovered during implementation, such as an admin detail endpoint.

### Decision 3: Admin console is list-and-detail oriented

The admin console will provide:

- a dedicated list page
- status, user, pile, and date filters where supported by the backend
- pagination
- a dedicated detail page
- router and top navigation integration

This round does not add admin-side mutation of charging records.

## Target State

### Backend

The charging-record backend continues to expose stable list and detail flows, with a dedicated admin detail read path if required by the admin frontend.

### Frontend Admin

`frontend-admin` gains a dedicated charging-record module:

- `GET /charging-record/admin/all`
- `GET /charging-record/admin/{id}`
- admin API, types, store, routes, and views

### Documentation

OpenSpec tasks and module baseline docs are updated to reflect that backend hardening is largely already landed and that the admin console is the main remaining gap addressed here.

## Validation

1. admin can open the charging-record list page
2. filters and pagination work against the backend contract
3. admin can open a dedicated detail page for a record
4. no regression is introduced into existing user-side charging-record flows