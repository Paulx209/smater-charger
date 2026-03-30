# Proposal: charging-record-admin-console-and-hardening

## Context

The charging-record module is partially out of sync between code and planning artifacts.
Backend hardening work that older docs still describe as pending is already present in code, while the admin frontend still lacks a dedicated charging-record console.

Current code reality:

- backend already exposes `/charging-record/admin/all`
- backend already has Redisson locking and transactional start-charging split
- backend already batch-fetches pile and vehicle data on list paths
- admin can only inspect charging records indirectly through user-management drawers

## Change Summary

This change will:

- create a focused OpenSpec record for the real remaining work
- keep existing backend hardening that is already implemented
- add only the missing backend support required by the admin console
- add a dedicated admin charging-record list and detail flow in `frontend-admin`
- update module baseline and tracking docs after implementation

## Non-Goals

- redesign the client charging-record experience
- refactor payment or billing logic
- refactor reservation workflows
- add realtime dashboards or websocket features
- clean up unrelated repository-wide TypeScript issues