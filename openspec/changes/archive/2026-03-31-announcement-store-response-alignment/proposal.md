# Proposal: announcement-store-response-alignment

## Context

The admin announcement module still treats API results like raw `AxiosResponse` objects even though the shared request wrapper already unwraps response data.
This creates avoidable type errors in the announcement store and pages.

## Change Summary

This change aligns the admin announcement API, store, and pages to the real unwrapped response contract.

## Non-Goals

- frontend-client announcement changes
- backend changes
- non-announcement admin fixes