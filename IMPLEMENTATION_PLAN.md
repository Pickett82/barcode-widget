# Barcode Widget Android App - Implementation Plan

## Objective
Build an Android app (Kotlin + Jetpack Compose) for loyalty card barcodes with a fast, offline-first, resizable and scrollable home-screen widget.

## Product Requirements
- Add loyalty cards by scanning barcode or typing barcode number.
- Choose store from a known-store list. Logo is auto-assigned.
- Waitrose is only an example. Behavior must work for any known store.
- Users are not required to upload images for known stores.
- Users can add custom stores and optionally provide custom logos.
- Store data must persist locally and work offline.
- Optional Google account backup/sync may be added later.
- Home-screen widget must:
	- show a list of loyalty cards using store logos
	- be resizable
	- be scrollable
	- keep pinned cards first (manual order)
	- then order remaining cards by usage/recency
	- open selected card barcode immediately on tap (no blocking loading screen)

## Architecture Decisions
- UI: Kotlin + Jetpack Compose.
- Storage: Room local database (local-first source of truth).
- Store catalog: bundled known-store catalog file with alias mapping.
- Branding: built-in logo mapping for known stores; optional custom logo URI for custom stores.
- Widget: collection-based AppWidget with RemoteViews for mature scroll behavior.
- Barcode rendering: precompute/cache where possible to minimize tap-to-display latency.

## Phase Plan
1. Foundation
- Create Gradle Android project, app module, Compose setup, lint/test baselines.
- Add GitHub Actions workflow skeleton for debug APK artifact.

2. Data + Domain
- Create Room schema for loyalty cards, pin rank, usage, timestamps, store and logo fields.
- Add DAO ordering queries (pinned first, then usage desc, then recency desc).
- Implement repository and usage increment path.

3. Known-Store Catalog Framework
- Add `store_catalog.json` schema and loader.
- Implement alias normalization to canonical store names.
- Implement logo asset resolution.
- Keep catalog expandable so store/logo list can be added later in development.

4. Add Card UX
- Flow: scan/type barcode -> pick known store -> save card.
- Optional custom store flow with optional custom image.
- Enforce no required image upload for known stores.

5. Card Display UX
- Scrollable card list in app.
- Barcode detail view with large scannable barcode and fallback text.
- Increment usage on open/use.

6. Widget UX
- Resizable/scrollable logo-first widget list.
- Tap logo deep-links directly to barcode detail.
- Offline operation from local state.
- Widget refresh on create/update/delete and usage updates.

7. Performance Hardening
- Target low tap-to-barcode latency.
- Cache warm-up and barcode bitmap caching.
- Avoid blocking splash/loading for deep-link open when local data exists.

8. Tests (Red-Green-Refactor)
- Unit tests: ordering, alias normalization, auto-logo assignment, repository logic.
- Instrumented/UI tests: add flow, widget tap flow, offline behavior.
- Performance checks for checkout speed path.

9. CI/CD
- GitHub Actions: run tests and build debug APK.
- Upload debug APK as downloadable artifact for testers.

## Initial Backlog
- [ ] Scaffold Android project files.
- [ ] Implement Room models and DAO.
- [ ] Implement known-store catalog loader.
- [ ] Build add-card flow with store selection.
- [ ] Build card list + detail barcode screen.
- [ ] Build widget provider + list service.
- [ ] Add unit/instrumented test skeletons.
- [ ] Add GitHub Actions workflow producing debug APK artifact.
- [ ] Document local run/test instructions.

## Acceptance Criteria
- Selecting any known store auto-assigns canonical store name and logo.
- Known stores never require image upload.
- Custom store path supports optional custom image.
- Widget is resizable and scrollable.
- Widget orders pinned cards first, then usage/recency.
- Widget tap opens correct barcode quickly and offline.
- CI run publishes downloadable debug APK artifact.
