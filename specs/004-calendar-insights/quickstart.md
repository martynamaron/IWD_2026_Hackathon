# Quickstart: Calendar Insights Panel

**Feature Branch**: `004-calendar-insights` | **Date**: 2026-03-13  
**Builds on**: `003-rich-data-types` (must be merged first)

---

## Prerequisites

- Android Studio Meerkat or later
- JDK 17+ (configured in `gradle.properties`)
- Android device or emulator running API 24+
- Specs 001 and 003 implementations complete and on `main`

---

## Key Files — What Changes and What Is New

### New files

| File | Purpose |
|---|---|
| `data/local/InsightEntity.kt` | Room entity for persisted insight observations |
| `data/local/InsightDao.kt` | DAO for insight CRUD and top-N queries |
| `data/local/AnalysisMetadataEntity.kt` | Single-row entity tracking last analysis state |
| `data/local/AnalysisMetadataDao.kt` | DAO for analysis metadata upsert/read |
| `data/repository/InsightRepository.kt` | Wraps insight + metadata DAOs; owns re-analysis decision logic |
| `data/analysis/CorrelationEngine.kt` | Pure Kotlin statistical correlation engine (Phi, Point-biserial, Pearson) |
| `data/analysis/InsightTextGenerator.kt` | Templated sentence generator for human-readable insights |
| `ui/components/InsightsPanel.kt` | Composable: insights panel with loading/content/empty/error states |
| `ui/components/InsightCard.kt` | Composable: single insight card with emoji + text |
| `ui/components/InsightLoader.kt` | Custom animated dots loader (not platform progress bar) |
| `viewmodel/InsightViewModel.kt` | ViewModel managing insight analysis lifecycle and UI state |

### Modified files

| File | Change |
|---|---|
| `data/local/AppDatabase.kt` | Version 2→3, add `InsightEntity` + `AnalysisMetadataEntity` to entities, add `AutoMigration(2, 3)`, expose new DAOs |
| `data/local/DailyEntryDao.kt` | + `getTotalCount()` and `getAllEntries()` queries for analysis |
| `data/local/MultiChoiceSelectionDao.kt` | + `getTotalCount()` and `getAllSelections()` queries for analysis |
| `ui/screens/calendar/CalendarScreen.kt` | Add `InsightsPanel` below calendar grid, add `verticalScroll` to Column |
| `viewmodel/CalendarViewModel.kt` | No changes — insights managed by separate `InsightViewModel` |
| `ui/navigation/NavGraph.kt` | Pass `InsightViewModel` factory to `CalendarScreen` route |
| `BioGraphApplication.kt` | Instantiate `InsightRepository` and pass to ViewModel factory |

---

## Room Migration: v2 → v3

Room auto-migration handles all schema changes. The `@AutoMigration(from = 2, to = 3)` annotation is all that is needed; no manual `Migration` object is required.

**What the migration adds:**
1. New table `insights` — stores computed insight observations with FK references to `data_types`.
2. New table `analysis_metadata` — single-row table tracking last analysis timestamp and data count.

No existing tables or columns are modified.

---

## Correlation Engine Overview

The `CorrelationEngine` is a stateless pure Kotlin class that:

1. Collects all daily entries and multi-choice selections
2. Builds a day-indexed matrix of values per data type
3. Computes pairwise correlations for every data type combination:
   - Toggle ↔ Toggle → Phi coefficient
   - Toggle → Scale → Point-biserial correlation
   - Scale ↔ Scale → Pearson correlation
   - MC option (as binary) → Toggle/Scale → same as above per option
4. Filters results to `|coefficient| ≥ 0.35` (~60% co-occurrence threshold)
5. Generates templated insight text with emoji interpolation
6. Returns sorted results (strongest correlation first)

**Minimum data requirements**: 7 days of entries across ≥2 data types. Below this threshold, the engine returns empty and the UI shows the "keep tracking" message.

---

## UI Architecture

### InsightPanelState (sealed interface)

```
Hidden          → No data types exist at all
InsufficientData → < 7 days or < 2 data types
Loading         → Analysis in progress
Success         → Insights ready to display
Error           → Analysis failed
```

### Screen Layout

```
CalendarScreen (Scaffold)
└── Column (verticalScroll)
    ├── Month navigation header
    ├── Day-of-week headers
    ├── AnimatedContent → CalendarGrid
    └── InsightsPanel (NEW)          ← inserted here
        ├── PrimaryTabRow: [Last month | Last 3 months | Last year]
        ├── Loading: InsightLoader (custom animated dots)
        ├── Success: InsightCard list + disclaimer text
        ├── InsufficientData: encouragement message
        └── Error: retry message
```

### Custom Loader

The `InsightLoader` uses `rememberInfiniteTransition` with 3 bouncing dots:
- Each dot: `tween(600ms, EaseInOutCubic)` with 100ms stagger
- Subtext: "Discovering patterns..." in `bodySmall`
- Transition to content: `AnimatedContent` with `fadeIn + fadeOut` (300ms)

---

## How to Verify

1. **Seed data**: The app already generates 2 months of correlated mock data on fresh install (e.g., headaches correlate with not leaving the house, mood correlates with seeing friends).
2. **Open Calendar**: Insights panel should appear below the grid with time period tabs, showing a custom loader briefly, then revealing insights.
3. **Switch time periods**: Tap each tab (Last month, Last 3 months, Last year). Verify insights update per the selected range. "Last month" may show fewer or different insights than "Last 3 months".
4. **Add new data**: Log a day's entry via the DayPanel. Return to Calendar. Verify insights re-compute (loader appears, new insights shown).
5. **Delete a data type**: Verify insights referencing it disappear.
6. **Fresh install with < 7 days**: Verify "Keep tracking!" message appears instead of insights.
