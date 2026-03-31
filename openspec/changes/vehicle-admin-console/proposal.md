# Proposal: vehicle-admin-console

## Context

The vehicle module already has backend and client-side functionality, but the admin app still lacks a dedicated vehicle console.
Administrators cannot query vehicles globally, inspect vehicle details, or remove invalid vehicle records from a standalone management entry.

## Change Summary

This change adds a focused admin vehicle console:

- backend admin vehicle list, detail, and delete endpoints
- `frontend-admin` vehicle list and detail pages
- filters by user ID and license plate

## Non-Goals

- vehicle audit workflows
- vehicle media uploads
- import/export
- admin-side default vehicle editing
