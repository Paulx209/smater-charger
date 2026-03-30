# Project Re-Identification Implementation Plan

## Scope

This plan covers only the baseline-recovery stage for the current project.

Included:

- Re-identify the current project by first-level business modules
- Compare code reality across backend, admin frontend, and client frontend
- Identify drift between code, project documents, and existing OpenSpec artifacts
- Update the main project inventory document
- Prepare a clean entry point for future module-scoped OpenSpec changes

Excluded:

- New feature implementation
- Cross-module refactors
- Large-scale document rewrites unrelated to current factual drift

## Objective

Restore a trustworthy project baseline so future development can proceed module by module with OpenSpec and Codex in a controlled way.

## Deliverables

1. A first-level module inventory based on code
2. A gap matrix covering:
   - backend
   - frontend-admin
   - frontend-client
3. A difference report between:
   - code
   - `接口文档设计/`
   - `openspec/`
4. An updated `接口文档设计/待实现功能清单.md`
5. A prioritized recommendation for which module should enter the next OpenSpec change

## Module Set

The initial recognition pass should cover these first-level business modules:

- User and authentication
- Vehicle management
- Reservation
- Charging pile
- Charging record
- Announcement
- Price configuration
- Fault report
- Warning notice
- Statistics and dashboard

## Execution Strategy

### Phase 1: Codebase Inventory

Goal:

- Identify where each module exists in backend, admin frontend, and client frontend

Actions:

- Scan controllers, services, repositories, DTOs, routes, views, stores, and API clients
- Map code artifacts to first-level business modules
- Ignore generated output under `target/` when judging implementation status

Completion criteria:

- Every module has a traceable code footprint or an explicit "not implemented" result

### Phase 2: Surface Status Matrix

Goal:

- Determine actual delivery status per module and per product surface

Status values:

- Implemented and end-to-end
- Backend only
- Admin frontend only
- Client frontend only
- Partially implemented
- Not implemented

Actions:

- Check whether backend APIs exist
- Check whether admin UI exists
- Check whether client UI exists
- Check whether the surfaces appear connected rather than isolated

Completion criteria:

- Each first-level module has one summarized status line with supporting evidence

### Phase 3: Document Drift Analysis

Goal:

- Compare code facts against current project documents and OpenSpec artifacts

Primary comparison targets:

- `接口文档设计/待实现功能清单.md`
- Other module-related documents under `接口文档设计/`
- `openspec/changes/`

Difference categories:

- Code exists but document says missing
- Document says complete but code is partial
- Backend exists but frontend missing
- Frontend exists but backend missing
- OpenSpec change exists but actual implementation state has diverged

Completion criteria:

- Every detected mismatch is categorized and attached to a module

### Phase 4: Project Inventory Rewrite

Goal:

- Turn the existing project tracking file into a usable module inventory

Primary file:

- `接口文档设计/待实现功能清单.md`

Target structure per module:

- Module name
- Backend status
- Admin frontend status
- Client frontend status
- Current gap
- Suggested next step

Rules:

- Write from code-confirmed facts only
- If something is uncertain, mark it as uncertain rather than assuming

Completion criteria:

- The file works as a planning board rather than a stale to-do note

### Phase 5: OpenSpec Recovery Recommendation

Goal:

- Decide how future changes should be created after baseline recovery

Actions:

- Review whether existing `openspec/changes/` items should be kept, revised, or archived
- Recommend one next module for focused change planning
- Recommend one next capability inside that module

Completion criteria:

- One clear recommended next change candidate is identified

## Work Breakdown

### Task Group A: Recognition

1. Map backend artifacts to first-level modules
2. Map `frontend-admin` artifacts to first-level modules
3. Map `frontend-client` artifacts to first-level modules
4. Build a module-to-artifact reference table

### Task Group B: Status Classification

1. Classify backend implementation status per module
2. Classify admin frontend implementation status per module
3. Classify client frontend implementation status per module
4. Determine whether each module is truly end-to-end or only partially connected

### Task Group C: Drift Detection

1. Read existing planning documents under `接口文档设计/`
2. Read existing OpenSpec changes
3. Compare documented intent against code reality
4. Produce a module-based difference list

### Task Group D: Document Update

1. Rewrite `接口文档设计/待实现功能清单.md` into a module inventory table or equivalent structured format
2. Update any directly conflicting high-value documents when the mismatch is obvious
3. Leave uncertain documents for later module-specific correction if needed

### Task Group E: Next-Step Recommendation

1. Rank modules by urgency and clarity
2. Identify which module is the best next candidate for a focused OpenSpec change
3. Recommend the smallest useful next capability inside that module

## Output Format Requirements

The recognition result should be easy to review quickly.

Required sections:

1. Executive summary
2. Module status matrix
3. Difference list
4. Documents updated
5. Recommended next module
6. Recommended next OpenSpec change

## Validation

Before closing this plan, verify:

1. All first-level modules were assessed
2. No module status is based only on document claims
3. The updated inventory file reflects code-first judgment
4. Recommended next work is narrow enough for one future OpenSpec change

## Risks

### Risk 1: Generated artifacts may distort recognition

Mitigation:

- Treat `src/` and frontend source files as primary evidence
- Ignore `target/` when deciding feature completeness

### Risk 2: Existing documents may be internally inconsistent

Mitigation:

- Compare every major claim against code before carrying it forward

### Risk 3: Module boundaries may overlap

Mitigation:

- Classify by dominant business purpose
- Record overlaps in notes instead of merging modules too early

### Risk 4: Recognition may become too detailed too early

Mitigation:

- Stay at first-level module granularity in this phase
- Delay sub-capability breakdown until one module is chosen for change planning

## Exit Condition

This plan is complete when:

- The project has a trustworthy first-level module inventory
- The main inventory document has been updated
- The user can select the next module from a clean factual baseline
- Future OpenSpec changes can be created from confirmed module reality rather than stale assumptions
