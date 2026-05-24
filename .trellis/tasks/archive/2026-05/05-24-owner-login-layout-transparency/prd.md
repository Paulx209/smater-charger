# Fix Owner Login Page First-Viewport Fit And Transparency

## Goal

Adjust the owner-facing login page at `/login` so the complete page fits within a single viewport on common desktop browser heights, while making the right-side login area more transparent without changing login behavior.

## Requirements

* Keep the owner login route and authentication behavior unchanged.
* Reduce fixed vertical sizing in `frontend-client/src/views/Login.vue` so the login page can fit in one viewport.
* Preserve the existing two-column desktop visual direction and mobile stacked layout.
* Increase transparency on the right-side login panel/card while keeping form labels, inputs, and actions readable.

## Acceptance Criteria

* [x] `/login` does not require vertical scrolling on a common desktop viewport such as 1366x768.
* [x] The right-side login area uses a visibly more translucent surface than before.
* [x] Login form controls, agreement checkbox, and links remain usable and readable.
* [x] Existing login validation and submit logic are not changed.
* [x] Frontend type-check/build passes for `frontend-client`.

## Definition of Done

* Relevant frontend guidelines are checked before coding.
* Implementation is limited to the owner login page unless verification shows shared styles must change.
* Build/type-check is run after changes.
* Local browser verification covers the `/login` page.

## Technical Approach

This is a single-view style adjustment. The implementation should replace fixed desktop minimum heights and large vertical spacing with viewport-aware `clamp()` values, cap the card height to fit the panel, and tune transparency/backdrop filters for the right side.

## Out of Scope

* Backend/auth API changes.
* Admin login page changes.
* Register page redesign.
* Copy/text changes unrelated to layout.

## Technical Notes

* Main impacted file: `frontend-client/src/views/Login.vue`.
* Frontend app uses Vue 3, Vite, Element Plus, Pinia, and vue-router.
* `/login` is routed from `frontend-client/src/router/index.ts`.
* Verified with Chrome headless screenshots at 1366x768 and 1280x720.
* `npm run build`, targeted `npx eslint src/views/Login.vue --no-fix --cache=false`,
  and `npm run test:unit -- --run` passed.
