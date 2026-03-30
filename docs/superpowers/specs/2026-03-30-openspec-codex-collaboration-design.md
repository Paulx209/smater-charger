# OpenSpec + Codex Collaboration Design

## Background

The current project has drifted away from its original planning documents. Existing implementation progress, pending work, and documentation are no longer aligned. The repository already contains:

- Multiple product surfaces: `backend`, `frontend-admin`, `frontend-client`
- Existing OpenSpec artifacts under `openspec/`
- Existing planning and API documents under `接口文档设计/`

The primary problem is not the absence of specifications. The primary problem is that the factual baseline has drifted. Future development should therefore start by re-identifying the current project state from code, then restoring document and OpenSpec reliability.

## Goals

1. Rebuild a trustworthy project baseline using code as the source of truth
2. Reclassify the project by first-level business modules rather than by repository directories
3. Identify implementation gaps across backend, admin frontend, and client frontend
4. Update the high-level project tracking document so it reflects actual progress
5. Reuse OpenSpec only for concrete future changes, not for storing the entire current system truth
6. Establish a repeatable collaboration workflow between the user and Codex

## Non-Goals

- Rewriting the entire planning system before re-identification
- Creating one mega-spec that covers all modules at once
- Starting feature implementation before the factual baseline is rebuilt
- Treating old documents as authoritative when they conflict with code

## Decisions

### 1. Source of Truth

Code is the default source of truth.

When code conflicts with existing documents, OpenSpec artifacts, or informal notes:

- Treat code as the current factual state
- Treat documents as needing correction
- Use documents only as supporting clues during analysis

### 2. Recognition Scope

The project should be re-identified by first-level business modules.

Recommended module list:

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

### 3. Codex Responsibility Boundary

Codex should not directly move from ambiguity into implementation.

For this project, Codex is responsible for:

- Scanning the codebase
- Producing module recognition results
- Comparing code against documents and OpenSpec
- Producing a difference report
- Drafting updated documentation

The user remains responsible for:

- Confirming the recognition result
- Choosing which module to advance next
- Approving scope before implementation starts

### 4. Document Layering

The workflow should use three distinct documentation layers.

#### A. Code Facts Layer

Purpose:

- Describe the current real state of the system

Characteristics:

- Derived from code
- Organized by first-level business modules
- Marks whether each module is fully implemented, partially implemented, backend-only, frontend-only, or not implemented

#### B. Project Overview Layer

Primary file:

- `接口文档设计/待实现功能清单.md`

Purpose:

- Act as the central project inventory and discrepancy board

Required columns or sections per module:

- Module name
- Backend status
- Admin frontend status
- Client frontend status
- Gap summary
- Recommended next step

This file should become the main entry point for planning work with Codex.

#### C. Change Planning Layer

Primary location:

- `openspec/changes/`

Purpose:

- Describe the next concrete change to be made

Rules:

- One change should cover one focused capability
- Do not mix multiple first-level modules in one change unless a cross-module dependency is explicit and unavoidable
- Do not use OpenSpec as a dump of the whole current system state

## Recommended Workflow

### Phase 1: Project Re-Identification

Codex scans the repository and produces a first-level module inventory using code as the baseline.

Required output:

- Module list
- Implementation status per surface
- Backend/admin/client differences
- Document drift summary

### Phase 2: Baseline Correction

Codex updates the project overview document to reflect actual implementation status.

Primary target:

- `接口文档设计/待实现功能清单.md`

If needed, supporting documents may also be updated to match confirmed reality.

### Phase 3: Module Selection

The user selects one module from the updated inventory as the next active work area.

Selection criteria may include:

- Highest business value
- Largest backend/frontend gap
- Most urgent missing capability
- Lowest uncertainty for fast progress

### Phase 4: OpenSpec Change Creation

Only after a module baseline is confirmed should Codex create or revise an OpenSpec change.

Each change should include:

- `proposal.md`
- `design.md`
- `tasks.md`

Each change should be narrow enough to support implementation in small steps.

### Phase 5: Task-Driven Implementation

Implementation should follow approved tasks, not vague feature requests.

Each implementation round should:

- Target a small subset of tasks
- Report what changed
- Report what was verified
- Update documentation if the implementation changes system truth

## Collaboration Templates

### A. Module Recognition Prompt

Use when the project baseline is unclear.

Example:

> Re-identify the current project by first-level business modules, using code as the source of truth. Compare backend, frontend-admin, frontend-client, and existing documents/OpenSpec. Output what is implemented, partially implemented, and missing. Do not write code yet.

Expected Codex output:

- Module inventory
- Difference list
- Priority suggestions
- Documents to update

### B. Module Alignment Prompt

Use when focusing on one module before creating a change.

Example:

> Based on the recognition result, analyze only the reservation module. Clarify what is implemented, what is missing, where backend/frontend are misaligned, and what should be corrected in project documents. Do not implement yet.

Expected Codex output:

- Module status
- Gap list
- Frontend/backend mismatch points
- Recommendation on whether a new OpenSpec change is needed

### C. OpenSpec Prompt

Use after a module baseline is confirmed.

Example:

> Based on the confirmed reservation module baseline, create an OpenSpec change for reservation cancellation and expiration handling only. Produce proposal, design, and tasks for that scoped change.

Expected Codex output:

- One focused change
- Clear scope boundary
- Actionable tasks

### D. Implementation Prompt

Use after a change is approved.

Example:

> Implement tasks 1-3 from the approved change. Keep the scope limited to those tasks. Report code changes, validation results, remaining tasks, and any required document updates.

Expected Codex output:

- Code changes
- Validation summary
- Remaining work
- Documentation sync notes

## Error Handling and Governance

To keep the process stable, enforce the following rules:

- Do not start coding from a vague request like "continue developing this project"
- Do not create one change that spans unrelated modules
- Do not let document updates lag behind confirmed implementation changes
- Do not treat historical planning text as authoritative over code
- When recognition results are uncertain, stop at the difference report and ask for confirmation before creating specs

## Testing and Verification Strategy

This design describes process, not runtime code. The process itself should still include verification gates.

Required verification checkpoints:

1. After module recognition:
   - Check whether all first-level modules are covered
   - Check whether each module has a clear three-surface status
2. After document updates:
   - Check whether the overview document matches confirmed code reality
3. After OpenSpec generation:
   - Check whether the change scope is narrow and single-purpose
4. After implementation:
   - Check whether completed tasks, code changes, and docs remain aligned

## Final Recommendation

Use an audit-first workflow.

The project should proceed in this order:

1. Re-identify all first-level business modules from code
2. Update `接口文档设计/待实现功能清单.md` into a real module inventory
3. Choose one module to advance
4. Create one focused OpenSpec change for one capability inside that module
5. Implement by approved tasks only

This restores trust in the project baseline first, then allows OpenSpec and Codex to work as intended.
