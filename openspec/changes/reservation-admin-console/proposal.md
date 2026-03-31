# Proposal: reservation-admin-console

## Context

The reservation module already has backend and client-side user flows.
The missing capability is a dedicated admin reservation console.
Right now admins can only see reservation data indirectly from the user detail drawer.

## Change Summary

This change adds a dedicated admin reservation console with:

- admin reservation list access
- admin reservation detail access
- admin cancellation for `PENDING` reservations only

## Non-Goals

- changing the client reservation flow
- redesigning reservation rules
- rewriting expiration handling or locking