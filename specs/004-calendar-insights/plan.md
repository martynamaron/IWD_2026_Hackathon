# Implementation Plan: Calendar Insights Panel

**Branch**: `004-calendar-insights` | **Date**: 2026-03-13 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-calendar-insights/spec.md`

## Summary

Add an insights panel to the bottom of the Calendar screen that surfaces pattern correlations from the user's tracked data. A stateless, pure-Kotlin correlation engine computes pairwise statistical correlations (Phi coefficient, Point-biserial, Pearson) across all data type combinations (Toggle, Scale, Multiple Choice). Results are persisted to the local Room database and displayed as templated plain-language sentences with emoji. A custom animated loader (bouncing dots, not platform progress bar) provides a delightful loading state. A persistent non-medical disclaimer is shown. Re-analysis triggers only when new data has been logged since the last analysis run. No network calls — all processing is on-device.

## Technical Context

**Language/Version**: Kotlin 2.0.21 (Compose compiler plugin)
**Primary Dependencies**: Jetpack Compose BOM 2026.03.00, Material 3, Room 2.7.0, Navigation Compose, kotlinx-serialization-json (all already present — zero new external dependencies)
**Storage**: Room (SQLite) — on-device only; DB version bumped 2 → 3 via auto-migration
**Testing**: Not mandated (constitution Principle IV); JUnit available in the project
**Target Platform**: Android (minSdk 24, targetSdk 36)
**Project Type**: Mobile app (single-activity Compose)
**Performance Goals**: 60 fps UI; insights visible within 10 seconds of screen open (SC-001); cached insights shown under 1 second (SC-005); custom loader visible within 500ms (SC-004)
**Constraints**: Offline-only, single-user, no new external dependencies, on-device analysis only (FR-018), no network calls
**Scale/Scope**: 2 new Room entities, 2 new DAOs, 1 new repository, 1 correlation engine, 1 text generator, 3 new Compose components, 1 new ViewModel, 2 modified screens/DAOs

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Design Gate

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Material 3 Design Consistency | ✅ PASS | Insights panel uses `Card`, `Text` from M3. Disclaimer uses `bodySmall` from centralized Typography. Colors from `MaterialTheme.colorScheme`. No hardcoded colors. Custom loader uses `Box` + `CircleShape` — not a custom Canvas drawing. |
| II. Compose-First UI Architecture | ✅ PASS | All new UI in Jetpack Compose. `InsightsPanel`, `InsightCard`, `InsightLoader` are stateless composables with hoisted state. Screen composable receives ViewModel. |
| III. Clean Kotlin Structure | ✅ PASS | MVVM maintained: new entities/DAOs in `data/local/`, new repository in `data/repository/`, correlation engine in `data/analysis/`, new ViewModel in `viewmodel/`. Sealed interface for panel state. |
| IV. Simplicity & Pragmatism | ✅ PASS | Zero new external dependencies. Pure Kotlin statistical analysis — no ML/stats library. Room auto-migration. Template-based insight text. On-device LLM deferred to follow-up (YAGNI). |
| V. Local Database | ✅ PASS | Room auto-migration v2→v3. New entities as `@Entity` data classes. New DAOs as `@Dao` interfaces. All access via `InsightRepository`. CASCADE DELETE on FK handles orphan cleanup. |
| VI. Delight & Motion Design | ✅ PASS | Custom animated dots loader with `rememberInfiniteTransition`, `tween(600ms, EaseInOutCubic)`, 100ms stagger. `AnimatedContent` with `fadeIn + fadeOut` for loading → content transition. No abrupt swaps. |
| UI & Design Standards | ✅ PASS | Touch targets N/A (insights are read-only display). `sp` text via centralized Typography. M3 card for insights. `verticalScroll` ensures content accessible on small screens. Error state uses inline text per existing app pattern. |

**Pre-design gate result: PASS** — No violations. Proceed to Phase 0.

### Post-Design Re-evaluation (after Phase 1)

| Principle | Status | Post-Design Notes |
|-----------|--------|-------------------|
| I. Material 3 Design Consistency | ✅ PASS | `Card` (M3) for insight items. `bodySmall` for disclaimer. All colors from `MaterialTheme.colorScheme`. Custom loader uses basic Compose primitives (`Box`, `CircleShape`), not Canvas drawing. |
| II. Compose-First UI Architecture | ✅ PASS | Three new stateless composables (`InsightsPanel`, `InsightCard`, `InsightLoader`) in `ui/components/`. State hoisted to `InsightViewModel`. `@Preview` annotations on all. |
| III. Clean Kotlin Structure | ✅ PASS | `CorrelationEngine` is a pure Kotlin class with no Android deps — testable. `InsightRepository` owns data access. `InsightViewModel` exposes `StateFlow<InsightPanelState>`. Business logic in ViewModel/engine, never in composables. |
| IV. Simplicity & Pragmatism | ✅ PASS | No new dependencies. Two simple DB tables. Statistical analysis in ~200 lines of Kotlin. Template-based text. Separate ViewModel avoids bloating existing `CalendarViewModel`. |
| V. Local Database | ✅ PASS | `@AutoMigration(from = 2, to = 3)` adds two tables. CASCADE DELETE on both FK columns. `AnalysisMetadataEntity` single-row pattern for change detection. Repository layer maintained. |
| VI. Delight & Motion Design | ✅ PASS | Bouncing dots loader (3 dots, staggered `tween(600ms)`). `AnimatedContent` crossfade for state transitions. `AnimatedVisibility` for panel appear/disappear. |

**Post-design gate result: PASS** — No violations after design review.

## Project Structure

### Documentation (this feature)

```text
specs/004-calendar-insights/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # N/A — no external interfaces (mobile-only app)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
BioGraphApp/app/src/main/java/com/martynamaron/biograph/
├── data/
│   ├── analysis/
│   │   ├── CorrelationEngine.kt                         # NEW — pure Kotlin correlation analysis
│   │   └── InsightTextGenerator.kt                      # NEW — templated sentence builder
│   ├── local/
│   │   ├── AppDatabase.kt                               # MODIFIED — version 3, new entities/DAOs, AutoMigration(2,3)
│   │   ├── InsightEntity.kt                             # NEW — Room entity for insights
│   │   ├── InsightDao.kt                                # NEW — DAO for insight queries
│   │   ├── AnalysisMetadataEntity.kt                    # NEW — single-row analysis tracking
│   │   ├── AnalysisMetadataDao.kt                       # NEW — DAO for metadata upsert/read
│   │   ├── DailyEntryDao.kt                             # MODIFIED — + getTotalCount, getAllEntries
│   │   └── MultiChoiceSelectionDao.kt                   # MODIFIED — + getTotalCount, getAllSelections
│   └── repository/
│       └── InsightRepository.kt                         # NEW — insight persistence + re-analysis logic
├── viewmodel/
│   └── InsightViewModel.kt                              # NEW — manages analysis lifecycle + UI state
└── ui/
    ├── components/
    │   ├── InsightsPanel.kt                             # NEW — panel composable with state handling
    │   ├── InsightCard.kt                               # NEW — single insight display card
    │   └── InsightLoader.kt                             # NEW — custom animated dots loader
    └── screens/
        └── calendar/
            └── CalendarScreen.kt                        # MODIFIED — add InsightsPanel below grid, add verticalScroll
```

**Structure Decision**: All new files follow the existing structure convention established in specs 001 and 003. The `data/analysis/` package is new — it houses domain logic that is neither data-access (repository) nor UI (viewmodel). This keeps the CorrelationEngine testable and decoupled from Android framework classes.
