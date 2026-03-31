# Proposal: warning-notice-admin-alignment

## Context

The warning-notice module already has backend and client-side user flows.
The missing capability is a dedicated admin warning-notice console.
Right now admins do not have a global notification management entry.

## Change Summary

This change adds a dedicated admin warning-notice console with:

- admin warning-notice list access
- admin warning-notice detail access
- admin batch mark-as-read
- admin delete
- admin global threshold configuration

## Non-Goals

- changing the client notification flow
- redesigning warning generation rules
- adding websocket or sms delivery changes