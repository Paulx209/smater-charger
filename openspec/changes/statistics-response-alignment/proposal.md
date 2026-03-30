# Proposal: statistics-response-alignment

## Context

The admin statistics module still mixes wrapped-data assumptions with a raw binary export endpoint.
This creates avoidable type errors and leaves the export path misaligned with the backend response shape.

## Change Summary

This change aligns the admin statistics API and store to the real backend response contracts.

## Non-Goals

- charging pile icon fixes
- backend changes
- non-statistics admin fixes