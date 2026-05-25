# Frontend Quality Guidelines

Both frontend apps share the same build and quality tooling.

## Commands

Run commands from the relevant frontend app directory:

```bash
npm run type-check
npm run lint
npm run test:unit
npm run build
```

`npm run build` runs type-check and Vite build through `npm-run-all2`.

## Tooling

- Vite 7
- Vue 3.5
- TypeScript 5.9
- Vue Router 4
- Pinia 3
- Element Plus 2.13
- Axios 1.13
- ESLint 9 with Vue and TypeScript rules
- Prettier 3.8
- Vitest 4 with Vue Test Utils

Node engine requirement in both apps:
`^20.19.0 || >=22.12.0`.

## Current Test Coverage

Each app currently has only the scaffold-style component test:
`src/components/__tests__/HelloWorld.spec.ts`.

When changing real business behavior, add focused tests when practical, but
recognize that the existing suite is thin. At minimum, run type-check and build
for touched frontend apps.

## Code Review Checklist

- Does the page call stores/API modules instead of direct Axios?
- Are API return types explicit and aligned with backend payloads?
- Are auth/session failures still handled through `utils/request.ts` and router
  guards?
- Are loading states represented in stores and bound in the UI where needed?
- Are Element Plus components used consistently for forms, tables, dialogs,
  messages, empty states, tags, and pagination?
- Are status labels/colors centralized in `types/` rather than duplicated?
- Did the change update both admin and client apps if they share the same
  backend contract?
- For visual UI work, does the verified local page avoid non-product overlays
  such as Vue DevTools floating widgets or debugging panels?

## Formatting

Use the configured formatter and editor rules:

- two-space indent
- UTF-8
- LF endings
- final newline
- no trailing whitespace
- 100 character max line length

## Avoid

- Do not introduce a new UI library for controls already covered by Element
  Plus.
- Do not add new global state where local component state is enough.
- Do not leave route-level pages without error/loading paths for async data.
- Do not make backend contract changes without updating matching `types/`,
  `api/`, and affected stores/views in both apps.
- Do not leave development-only visual overlays enabled when the task requires
  screenshot-level UI matching on `localhost`; they make the rendered page
  differ from the target design.
