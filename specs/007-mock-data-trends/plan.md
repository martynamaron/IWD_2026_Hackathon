# Implementation Plan: Mock Data Generation & Insight Trends

**Branch**: `007-mock-data-trends` | **Date**: 13 March 2026 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/007-mock-data-trends/spec.md`

## Summary

Rewrite the mock data generator to produce 3 or 6 months of correlated health data across six new data types (Health, Medication, Mood, Energy Levels, Exercise, Period Bleeding) with five embedded correlation patterns (medication–dizziness, medication–mood, menstrual cycle–mood/energy, exercise–tiredness, and exercise–tiredness weakening over time). Add trend indicators ("↑ Strengthening", "↓ Weakening", "→ Stable") to insight cards in the 3-month and 1-year views by comparing correlation strength between the first and second halves of the viewed range, using a ≥15 percentage point threshold.

## Technical Context

**Language/Version**: Kotlin 2.0.21  
**Primary Dependencies**: Jetpack Compose (BOM 2026.03.00, Material 3), Room 2.7.0, Navigation Compose 2.8.4, Lifecycle ViewModel Compose 2.8.7  
**Storage**: Room (local SQLite, AppDatabase v4, auto-migrations)  
**Testing**: Not mandated (constitution Principle IV: Simplicity & Pragmatism)  
**Target Platform**: Android (minSdk 24, targetSdk 36)  
**Project Type**: Mobile app (Android, single-activity Compose)  
**Performance Goals**: Mock data generation within a few seconds; insights panel renders at 60fps  
**Constraints**: Fully offline; no server dependencies; Room auto-migrations only  
**Scale/Scope**: 6 data types, ~90–180 days of generated data, 5 correlation patterns, 1 new UI element (trend indicator)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Compliance | Notes |
|-----------|-----------|-------|
| I. Material 3 Design Consistency | ✅ PASS | Trend indicator uses `MaterialTheme.typography` and semantic colour tokens from ColorScheme. Arrow icons from Material icons set. |
| II. Compose-First UI Architecture | ✅ PASS | All new UI is Jetpack Compose composables. Trend state hoisted to ViewModel. |
| III. Clean Kotlin Structure | ✅ PASS | MockDataGenerator stays in `util/`, trend computation in `data/analysis/` or `viewmodel/`, UI in `ui/components/`. MVVM preserved. |
| IV. Simplicity & Pragmatism | ✅ PASS | No new abstractions. Generator rewritten in-place. Trend computed inline in ViewModel. No new dependencies. |
| V. Local Database | ✅ PASS | All data in Room. Trend indicators are computed dynamically, not stored. Silent replace uses existing DAO delete+insert. |
| VI. Delight & Motion Design | ✅ PASS | Trend indicators animate in with the existing `AnimatedContent` transitions on insight cards. |

**Gate result**: PASS — no violations. Proceeding to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/007-mock-data-trends/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (NOT created by plan)
```

### Source Code (repository root)

```text
BioGraphApp/app/src/main/java/com/martynamaron/biograph/
├── data/
│   ├── analysis/
│   │   ├── CorrelationEngine.kt      # Existing — no changes needed
│   │   └── InsightTextGenerator.kt   # Existing — no changes needed
│   └── local/
│       ├── InsightEntity.kt          # Existing — no schema changes
│       ├── DataTypeEntity.kt         # Existing — no schema changes
│       ├── DailyEntryEntity.kt       # Existing — no schema changes
│       └── AppDatabase.kt            # Existing — no migration needed
├── ui/
│   └── components/
│       ├── InsightCard.kt            # MODIFY — add optional trend indicator
│       ├── InsightsByStrength.kt     # MODIFY — pass trend data through
│       ├── InsightsByDataType.kt     # MODIFY — pass trend data through
│       └── InsightsPanel.kt          # MODIFY — pass trend data through
├── viewmodel/
│   └── InsightViewModel.kt           # MODIFY — add trend computation, time-range selection for mock data
└── util/
    └── MockDataGenerator.kt          # REWRITE — new data types, correlations, time range parameter
```

**Structure Decision**: Single Android project with feature modules organised by layer (data/ui/viewmodel/util). No new packages needed — all changes fit into existing structure.

## Complexity Tracking

No constitution violations — this section is intentionally empty.
