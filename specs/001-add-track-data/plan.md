# Implementation Plan: Add and Track Data

**Branch**: `001-add-track-data` | **Date**: 2026-03-12 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-add-track-data/spec.md`

## Summary

Build BioGraph's core feature: user-defined data types (emoji + description) with a calendar-based daily tracking interface. Users create custom data types, toggle them on/off per day from a monthly calendar view, and manage their types over time. Includes onboarding with pre-defined suggestions and mock data generation. Implementation uses Jetpack Compose for UI, Room for local persistence, and MVVM architecture with unidirectional data flow.

## Technical Context

**Language/Version**: Kotlin 2.0.21 (via Kotlin Compose plugin)
**Primary Dependencies**: Jetpack Compose (BOM 2026.03.00), Material 3, Room, Navigation Compose, Lifecycle ViewModel Compose
**Storage**: Room (SQLite) — on-device only, no cloud/server
**Testing**: Not mandated (per constitution Principle IV — Simplicity & Pragmatism), JUnit available
**Target Platform**: Android (minSdk 24, targetSdk 36)
**Project Type**: Mobile app (single-activity Compose)
**Performance Goals**: 60 fps UI, screen transitions < 1 second, calendar navigation < 1 second
**Constraints**: Offline-only, single-user, no authentication, up to 30 data types, 12+ months calendar history
**Scale/Scope**: Single user, ~5 screens (onboarding, calendar, day panel, data type management, settings), ~15 source files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Material 3 Design Consistency | ✅ PASS | All UI uses Material 3 components, `MyApplicationTheme`, centralised colors/typography. No custom-drawn elements. |
| II. Compose-First UI Architecture | ✅ PASS | All UI in Jetpack Compose. No XML layouts or Fragments. Stateless composables with state hoisting to ViewModels. |
| III. Clean Kotlin Structure | ✅ PASS | MVVM with `ui/screens/<feature>/`, `viewmodel/`, `data/` packages. StateFlow for UI state. Sealed classes for state. |
| IV. Simplicity & Pragmatism | ✅ PASS | No DI framework. Manual constructor injection. No unnecessary abstractions. Room is the only new dependency beyond starter template. |
| V. Local Database | ✅ PASS | Room for all persistence. Repository layer between DB and ViewModels. `@Entity` data classes, `@Dao` interfaces in `data/local/`. |
| VI. Delight & Motion Design | ✅ PASS | AnimatedNavHost for navigation. AnimatedVisibility for state changes. Staggered list animations. Ripple/scale feedback on interactive elements. |
| UI & Design Standards | ✅ PASS | Portrait orientation. 48dp touch targets. sp text sizing. Material 3 progress/error indicators. Animated transitions throughout. |

**Pre-design gate result: PASS** — No violations. Proceed to Phase 0.

### Post-Design Re-evaluation (after Phase 1)

| Principle | Status | Post-Design Notes |
|-----------|--------|-------------------|
| I. Material 3 Design Consistency | ✅ PASS | ModalBottomSheet, standard toggles, Snackbar for errors. Centralised theme. |
| II. Compose-First UI Architecture | ✅ PASS | Stateless composables, state hoisted to ViewModels. Reusable components in `ui/components/`. |
| III. Clean Kotlin Structure | ✅ PASS | MVVM: `ui/screens/`, `viewmodel/`, `data/`. StateFlow. Data classes + sealed classes. |
| IV. Simplicity & Pragmatism | ✅ PASS | 7 new deps, all justified. No DI framework. Custom calendar grid avoids unnecessary library. |
| V. Local Database | ✅ PASS | Room entities + DAOs in `data/local/`. Repository layer. Schema export enabled. |
| VI. Delight & Motion Design | ✅ PASS | AnimatedNavHost, AnimatedContent for month transitions, ModalBottomSheet animations. |

**Post-design gate result: PASS** — No violations after design review.

## Project Structure

### Documentation (this feature)

```text
specs/001-add-track-data/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (N/A — no external interfaces)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
BioGraphApp/app/src/main/java/com/martynamaron/biograph/
├── MainActivity.kt
├── BioGraphApplication.kt              # Application class for DB singleton
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt             # Room database definition
│   │   ├── DataTypeEntity.kt          # @Entity for data types
│   │   ├── DailyEntryEntity.kt        # @Entity for daily entries
│   │   ├── DataTypeDao.kt             # @Dao for data type CRUD
│   │   └── DailyEntryDao.kt           # @Dao for daily entry CRUD
│   └── repository/
│       ├── DataTypeRepository.kt       # Repository for data types
│       └── DailyEntryRepository.kt     # Repository for daily entries
├── viewmodel/
│   ├── CalendarViewModel.kt            # Calendar + day panel state
│   ├── DataTypeViewModel.kt            # Data type CRUD state
│   └── OnboardingViewModel.kt          # Onboarding flow state
├── ui/
│   ├── theme/
│   │   ├── Color.kt                    # (existing)
│   │   ├── Theme.kt                    # (existing)
│   │   └── Type.kt                     # (existing)
│   ├── navigation/
│   │   └── NavGraph.kt                 # AnimatedNavHost + routes
│   ├── screens/
│   │   ├── calendar/
│   │   │   ├── CalendarScreen.kt       # Monthly calendar view
│   │   │   └── DayPanel.kt            # Bottom sheet with toggles
│   │   ├── datatype/
│   │   │   ├── DataTypeListScreen.kt   # Manage data types
│   │   │   └── DataTypeEditDialog.kt   # Add/edit data type dialog
│   │   ├── onboarding/
│   │   │   └── OnboardingScreen.kt     # First-run onboarding
│   │   └── settings/
│   │       └── SettingsScreen.kt       # Mock data generation
│   └── components/
│       ├── EmojiPicker.kt              # Emoji selection component
│       ├── DataTypeToggleItem.kt       # Toggle row in day panel
│       └── CalendarDay.kt              # Single day cell in calendar
└── util/
    └── MockDataGenerator.kt            # 2-month random data seeder
```

**Structure Decision**: Standard Android single-module Compose app following the constitution's package layout: `ui/screens/<feature>/` for composables, `viewmodel/` for ViewModels, `data/` for Room entities, DAOs, and repositories. No multi-module split needed for this prototype scope.

## Complexity Tracking

> No constitution violations — this section is intentionally empty.
