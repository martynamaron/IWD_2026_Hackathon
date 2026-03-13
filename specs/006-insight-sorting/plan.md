# Implementation Plan: Insight Sorting

**Branch**: `006-insight-sorting` | **Date**: 13 March 2026 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/006-insight-sorting/spec.md`

## Summary

Add two sort modes to the existing Calendar Insights panel: "By Strength" (flat list, strongest correlations first — the default) and "By Data Type" (grouped under collapsible data-type headers). A segmented toggle control below the time-period tabs lets the user switch between modes with animated transitions. Each insight card gains a visual strength tier label ("Strong" / "Moderate") using semantic theme colours. The selected sort mode is persisted via Room.

## Technical Context

**Language/Version**: Kotlin 2.0.21  
**Primary Dependencies**: Jetpack Compose (Material 3), Room 2.7.0, Navigation Compose 2.8.4  
**Storage**: Room SQLite (on-device only)  
**Testing**: Not mandated (constitution Principle IV)  
**Target Platform**: Android (minSdk 24, targetSdk 36)  
**Project Type**: Mobile app (Android)  
**Performance Goals**: Sort mode switch < 300ms; insight list render at 60fps  
**Constraints**: On-device only, no network; follow M3 design; animated transitions required  
**Scale/Scope**: Typically < 20 insights at a time; < 15 data types

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Material 3 Design Consistency | ✅ PASS | Segmented toggle uses `SingleChoiceSegmentedButtonRow`. Colours from `MaterialTheme.colorScheme`. 4dp/8dp grid. |
| II. Compose-First UI Architecture | ✅ PASS | All new UI is composable. State hoisted to ViewModel. Side effects via `LaunchedEffect`. |
| III. Clean Kotlin Structure | ✅ PASS | MVVM with `StateFlow`. Sealed class for sort mode. No business logic in composables. |
| IV. Simplicity & Pragmatism | ✅ PASS | No new libraries. No DI framework. Sort preference stored as a single Room entity (reuses existing DB). |
| V. Local Database | ✅ PASS | Sort preference persisted in Room. No cloud. Auto-migration for schema v4. |
| VI. Delight & Motion Design | ✅ PASS | `AnimatedContent` for sort mode transitions. `AnimatedVisibility` for collapsible groups. 150–500ms tween with standard easing. |
| UI & Design Standards | ✅ PASS | All state changes animated. Touch targets ≥ 48dp via Material 3 defaults. |

**Pre-design gate: PASS — no violations.**

## Project Structure

### Documentation (this feature)

```text
specs/006-insight-sorting/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (created by /speckit.tasks)
```

### Source Code (repository root)

```text
BioGraphApp/app/src/main/java/com/martynamaron/biograph/
├── data/
│   └── local/
│       ├── InsightEntity.kt           # (existing — no changes needed)
│       ├── InsightDao.kt              # (existing — add query: getInsightsByDataType)
│       ├── UserPreferenceEntity.kt    # NEW — single-row key/value for sort pref
│       ├── UserPreferenceDao.kt       # NEW — upsert/get for sort preference
│       └── AppDatabase.kt            # Modified — add entity + dao + migration v3→v4
├── data/
│   └── repository/
│       └── UserPreferenceRepository.kt  # NEW — thin wrapper over DAO
├── viewmodel/
│   └── InsightViewModel.kt           # Modified — add sort mode state, grouping logic
├── ui/
│   ├── components/
│   │   ├── InsightsPanel.kt          # Modified — add sort toggle row, delegate to sort views
│   │   ├── InsightCard.kt            # Modified — add StrengthBadge composable
│   │   ├── StrengthBadge.kt          # NEW — "Strong"/"Moderate" label with colour accent
│   │   ├── InsightsByStrength.kt     # NEW — flat list sorted by coefficient
│   │   └── InsightsByDataType.kt     # NEW — grouped list with collapsible headers
│   └── theme/
│       └── Color.kt                  # Modified — add semantic strength tier tokens
└── BioGraphApplication.kt            # Modified — wire UserPreferenceRepository
```

**Structure Decision**: Feature follows the existing single-project MVVM structure. New files are minimal — 5 new files (1 entity, 1 DAO, 1 repository, 3 composables). All other changes are modifications to existing files.
