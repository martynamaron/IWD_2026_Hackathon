# Implementation Plan: Brand Theme Refresh

**Branch**: `005-brand-theme-refresh` | **Date**: 2026-03-13 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/005-brand-theme-refresh/spec.md`

## Summary

Replace the default purple/pink Material 3 colour scheme with a custom green brand palette (#15603E, #1E8D5A, #6CC19F) and grey (#D4D2D2) across the entire app. Disable dynamic colour (Material You) so brand colours always show. Add the leaf logo to the Onboarding screen (top-center) and Calendar top app bar (icon + wordmark). Update the launcher icon to use brand colours. Light mode only — dark mode is deferred.

## Technical Context

**Language/Version**: Kotlin (Jetpack Compose, Material 3)  
**Primary Dependencies**: Compose Material 3, Compose Navigation, Room  
**Storage**: Room SQLite (no changes needed for this feature)  
**Testing**: `@Preview` composables (no formal test framework mandated by constitution)  
**Target Platform**: Android (minSdk per project config)  
**Project Type**: Mobile app (Android)  
**Performance Goals**: 60fps UI rendering (no new performance concerns for theming)  
**Constraints**: Light mode only (dark mode deferred per clarification); WCAG AA contrast compliance  
**Scale/Scope**: 4 screens, ~12 UI components, 3 theme files, 2 XML resource files, 1 launcher icon set

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Material 3 Design Consistency | **PASS** | Colors defined in `ColorScheme` via `Color.kt`; no hardcoded colors in composables |
| II. Compose-First UI Architecture | **PASS** | Logo added as Compose `Image` composable; no XML layouts introduced |
| III. Clean Kotlin Structure | **PASS** | No ViewModel or data layer changes; changes limited to `ui/theme/` and screen composables |
| IV. Simplicity & Pragmatism | **PASS** | Minimal changes: 3 theme files + 2 screen composables + XML resources. No new abstractions |
| V. Local Database | **N/A** | No data layer changes |
| VI. Delight & Motion Design | **PASS** | Logo on Onboarding can use `AnimatedVisibility` fade-in per constitution requirements |
| UI & Design Standards | **PASS** | WCAG AA contrast verified in research; touch targets unchanged; `@Preview` annotations required for modified composables |
| Development Workflow | **PASS** | Working on dedicated branch `005-brand-theme-refresh`; commits will use `feat:` prefix |

**Gate result: PASS** — No violations. Proceed to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/005-brand-theme-refresh/
├── plan.md              # This file
├── research.md          # Phase 0: colour mapping & contrast research
├── data-model.md        # Phase 1: N/A (no data model changes — placeholder only)
├── quickstart.md        # Phase 1: implementation quickstart guide
├── checklists/
│   └── requirements.md  # Spec quality checklist
└── tasks.md             # Phase 2 output (NOT created by /speckit.plan)
```

### Source Code (files to modify)

```text
BioGraphApp/app/src/main/
├── java/com/martynamaron/biograph/ui/
│   ├── theme/
│   │   ├── Color.kt                    # Replace purple/pink with green palette
│   │   └── Theme.kt                    # Disable dynamic color; set light scheme
│   └── screens/
│       ├── onboarding/
│       │   └── OnboardingScreen.kt     # Add logo (top-center, above headline)
│       └── calendar/
│           └── CalendarScreen.kt       # Add icon + wordmark to TopAppBar title
└── res/
    ├── values/
    │   ├── colors.xml                  # Clean up unused purple/teal, keep greens
    │   └── ic_launcher_background.xml  # Already #D4D2D2 — no change needed
    └── drawable/
        ├── black_logo.xml              # Existing logo asset — used as-is
        └── ic_launcher_background.xml  # Replace #3DDC84 with brand green
```

**Structure Decision**: Android single-module mobile app. All changes are within the existing `app/` module — no new modules, packages, or projects introduced.

## Complexity Tracking

> No constitution violations. This table is intentionally empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| *(none)* | | |
