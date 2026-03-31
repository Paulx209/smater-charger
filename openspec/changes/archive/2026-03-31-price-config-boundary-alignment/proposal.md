# Proposal: price-config-boundary-alignment

## Context

The price-config module still leaks admin management responsibilities into the client app.
At the same time, backend routes do not clearly separate admin management from client-facing reads.

## Change Summary

This change aligns the price-config module to a clean boundary:

- admin management moves behind `/admin/price-config`
- client behavior is limited to current-price lookup and fee estimation
- client admin routes and pages are removed

## Non-Goals

- redesigning the pricing model
- dynamic pricing
- charging-record fee-calculation changes