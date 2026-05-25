# Owner Home UI Redesign

## Goal

Redesign the owner-facing home page to match the provided reference image: a warm orange-and-cream dashboard with left sidebar navigation, top account bar, vehicle summary, quick pile search, charging status, common functions, promotional/support blocks, and monthly charging statistics.

## What I Already Know

* User explicitly considers this an important page refactor and wants decisions confirmed before implementation.
* User expects JavaScript/business logic to remain mostly unchanged; the main change is frontend UI layout.
* Reference image shows a dashboard-style owner home page:
  * Left sidebar with logo, navigation items, notification badge, promo card, and customer service card.
  * Top content header with greeting, notification button, avatar/account label, and logout button.
  * Main content cards for current vehicle, quick pile search, current charging status, common functions, carbon reduction, and monthly charging statistics.
  * Warm white/orange visual style with soft borders, rounded cards, and light illustrations.
* User provided the vehicle-card background image in the conversation.
* Vehicle-card background asset path is confirmed as `frontend-client/public/images/home-vehicle-card-bg.png`, referenced in CSS as `/images/home-vehicle-card-bg.png`.
* User wants image-backed cards for the vehicle display area, carbon-reduction area, and the sidebar promo area above the customer hotline.
* Carbon-reduction and sidebar promo assets are not available yet; reserve image slots for later replacement.
* Current owner home page is `frontend-client/src/views/HomeView.vue`.
* Current authenticated app shell/top navigation is `frontend-client/src/App.vue`.
* Current home page composes `AnnouncementCarousel` and `CurrentChargingStatus`, then renders quick action cards and feature cards.
* Current charging-state logic lives in `frontend-client/src/components/CurrentChargingStatus.vue`.
* Unread notification count is already available from `useWarningNoticeStore().unreadCount`.
* Current user information is available through `useUserStore().userInfo` in `App.vue`.

## Assumptions To Validate

* The redesign applies to the logged-in owner app shell and home page, not the login/register pages.
* Existing route navigation and store/API calls should be reused where practical.
* New illustrative images can be approximated with CSS/gradients/icons unless real assets are provided or approved.
* The layout should be desktop web first; mobile-specific redesign is out of scope for this task.

## Open Questions

* None for implementation.

## Requirements Evolving

* Preserve existing authentication, logout, route guard, unread notification polling, and navigation behavior unless explicitly approved otherwise.
* Replace the logged-in owner app shell with a persistent left sidebar and top account bar matching the reference direction.
* Rework the owner home visual layout to follow the provided reference.
* Implement via an overall shell + home content replacement while reusing existing store/API logic.
* Add a waving expression after the `Hello，欢迎回来！` greeting.
* The vehicle display card must support a user-provided background image.
* The vehicle display card should reference `/images/home-vehicle-card-bg.png`.
* The carbon-reduction card must reserve a background image slot; initial implementation may use a placeholder visual until the user provides an asset.
* The left sidebar must include both a promotional image card and a customer hotline card below it.
* The promotional card above the hotline must reserve a background image slot; initial implementation may use a placeholder visual until the user provides an asset.
* Use existing real data where it already exists without backend changes:
  * current account display from `useUserStore`
  * unread notification count from `useWarningNoticeStore`
  * current charging status from existing charging-record current endpoint
  * monthly charging statistics from existing `fetchMonthlyStatistics(year, month)` / `/charging-record/statistics/monthly`
  * default vehicle display from existing `useVehicleStore().fetchMyVehicles()` and `defaultVehicle`
* If the user has no bound vehicle, the vehicle card must show: `暂未绑定车辆，前去绑定`, with a navigation action to vehicle management/binding.
* Vehicle range and battery percentage may use static visual values because no real state-of-charge/range fields exist in the current vehicle contract.
* Keep decorative/static blocks for content that has no confirmed existing endpoint yet, such as promo card, customer service card, and carbon-reduction copy.
* Desktop web layout is the target; mobile-specific navigation and layout polishing are not required in this task.
* Avoid backend/API contract changes unless later approved.
* Keep business logic changes minimal and intentional.

## Acceptance Criteria Evolving

* [x] Home page visually matches the provided reference direction.
* [x] Existing owner app navigation routes remain reachable.
* [x] Existing notification count and logout behavior still work.
* [x] Greeting includes a waving expression after `Hello，欢迎回来！`.
* [x] Current vehicle card shows the user's default vehicle when one exists.
* [x] Current vehicle card shows `暂未绑定车辆，前去绑定` when no vehicle is bound and links to vehicle management.
* [x] Current vehicle card uses static range/battery visual values when showing a bound vehicle.
* [x] Vehicle card renders with the vehicle-card background image.
* [x] Carbon-reduction and sidebar promo areas include replaceable image slots/placeholders.
* [x] Sidebar includes the customer hotline card below the promotional image card.
* [x] Current charging status behavior is preserved or an approved replacement is implemented.
* [x] Monthly charging statistics displays real current-month total count, total electric quantity, and total fee from the existing monthly statistics endpoint.
* [x] Build/type-check pass for `frontend-client`.
* [x] Browser screenshot review confirms desktop layout quality.

## Definition Of Done

* Requirements are approved by the user before implementation starts.
* Relevant frontend guidelines are read before coding.
* UI is implemented with Vue 3, Element Plus, scoped CSS, and existing project patterns.
* Targeted lint/type-check/build/test verification is run.
* Any work commits are separated from unrelated dirty files.

## Technical Approach Evolving

The redesign will update the authenticated owner shell globally rather than only the home page. `App.vue` should own the sidebar, top account/notification/logout area, and responsive shell behavior. `HomeView.vue` should focus on the dashboard content grid.

## Decision ADR-Lite

### Global Owner Shell

**Context**: The reference image shows a complete app dashboard frame, including persistent left navigation and account controls, not only home-page cards.

**Decision**: Replace the logged-in owner app's current top navigation with a global left sidebar plus top account bar.

**Consequences**: This creates a consistent owner dashboard experience across authenticated pages, but it expands scope from `HomeView.vue` to `App.vue` and requires checking that existing pages remain usable inside the new shell.

### Data Realism Boundary

**Context**: The user prefers minimal JavaScript/business-logic changes but noted that monthly charging statistics should already be available from existing records.

**Decision**: Use real existing data for account, unread notifications, default vehicle, current charging status, and current-month charging statistics. Keep other non-critical visual blocks static unless an existing endpoint is confirmed and using it does not expand the scope.

**Consequences**: The dashboard is more useful without backend changes, while avoiding a broad data-integration pass for every visual element in the reference.

### Default Vehicle Card

**Context**: The reference has a current-vehicle card. Existing frontend vehicle state already exposes the user's vehicle list and a computed default vehicle.

**Decision**: Load the user's vehicle list on the home page, display `defaultVehicle` when available, and show `暂未绑定车辆，前去绑定` with a vehicle-management CTA when the user has not bound a vehicle.

**Consequences**: The vehicle card uses real user identity data without backend changes. Real-time state-of-charge and driving range are intentionally static visual values for now because the existing vehicle type only exposes plate, brand, model, and battery capacity.

### Implementation Approach

**Context**: The target UI is a substantial visual refactor, but the user wants JavaScript/business logic to remain largely unchanged.

**Decision**: Use an overall shell + home content replacement approach. Update `App.vue` for the logged-in owner shell and `HomeView.vue` for the dashboard content while reusing the existing user, warning notice, vehicle, and charging record stores.

**Consequences**: The page can match the reference closely without a broad backend or business-logic rewrite. Existing child components may be bypassed or lightly reused if their current Element Plus card structure conflicts with the new visual layout.

## Out Of Scope

* Backend contract changes.
* Admin app changes.
* Login/register redesign.
* Mobile-specific redesign/polish.
* New payment, charging, or vehicle business logic unless explicitly approved.

## Technical Notes

* Likely affected files:
  * `frontend-client/src/views/HomeView.vue`
  * `frontend-client/src/App.vue`
  * Possibly `frontend-client/src/components/CurrentChargingStatus.vue` if the current status card must be restyled in place.
* Current project has many unrelated dirty files; implementation must avoid including unrelated changes.
* Implemented files:
  * `frontend-client/src/App.vue`
  * `frontend-client/src/views/HomeView.vue`
  * `frontend-client/vite.config.ts`
  * `frontend-client/public/images/home-vehicle-card-bg.png`
* Verification run:
  * `npm run type-check`
  * `npx eslint src/App.vue src/views/HomeView.vue vite.config.ts --no-fix --cache=false`
  * `npm run build`
  * `npm run test:unit -- --run`
  * `git diff --check -- frontend-client/src/App.vue frontend-client/src/views/HomeView.vue frontend-client/vite.config.ts .trellis/tasks/05-24-owner-home-ui-redesign/prd.md`
  * Browser screenshot through Chrome DevTools Protocol with mocked authenticated owner APIs.
* Quality review fixes applied after initial implementation:
  * Quick function cards are keyboard-focusable buttons instead of click-only articles.
  * Vehicle display now strictly uses `defaultVehicle`; a separate empty state handles users with
    vehicles but no default vehicle selected.
  * Removed the visible `/announcement` and `/about` sidebar secondary links because they did not
    exist in the supplied reference image and made the left sidebar inconsistent with the target UI.
  * Removed the visible monthly-statistics detail link to keep the bottom statistics card aligned
    with the supplied reference image.
  * Removed `vite-plugin-vue-devtools` from `frontend-client/vite.config.ts` because its dev overlay
    rendered a floating control on `localhost:5173` and polluted the target UI screenshot.
* Vehicle-card background asset exists at
  `frontend-client/public/images/home-vehicle-card-bg.png`; the build no longer emits the
  unresolved image URL warning.
* Browser screenshot verification:
  * URL: `http://127.0.0.1:5173/`
  * Viewport: `1536x1024`
  * Auth/data mode: Chrome DevTools Protocol request mocking for existing `/api/*` contracts.
  * Evidence: `scrollHeight=1024`, `clientHeight=1024`, `scrollWidth=1536`, `clientWidth=1536`,
    no failed requests, no devtools overlay, no visible sidebar extra links, and all primary
    dashboard text markers present.
  * Screenshot file: `C:\Users\Paul\AppData\Local\Temp\smart-charger-owner-home-dashboard-final-clean.png`.
* Short desktop viewport sizing verification after the page still scrolled on the user's machine:
  * Viewport `1536x900`: `scrollHeight=900`, `clientHeight=900`, `scrollWidth=1536`,
    `clientWidth=1536`.
  * Viewport `1366x768`: `scrollHeight=768`, `clientHeight=768`, `scrollWidth=1366`,
    `clientWidth=1366`.
  * Screenshot files:
    * `C:\Users\Paul\AppData\Local\Temp\smart-charger-home-1536x900.png`
    * `C:\Users\Paul\AppData\Local\Temp\smart-charger-home-1366x768.png`
  * User confirmed the updated layout display has no obvious issue.
