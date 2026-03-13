# Quickstart: Insight Sorting

**Feature**: 006-insight-sorting  
**Date**: 13 March 2026

## Prerequisites

- Android Studio with Kotlin 2.0.21+ and Compose BOM 2026.03.00
- BioGraphApp builds and runs successfully on current `006-insight-sorting` branch
- Spec 004 (Calendar Insights) is fully implemented (insights panel, correlation engine, Room schema v3)

## Implementation Order

### Step 1 — Data Layer (Room)

1. Create `UserPreferenceEntity.kt` in `data/local/`
2. Create `UserPreferenceDao.kt` in `data/local/`
3. Add entity and DAO to `AppDatabase.kt` + auto-migration v3 → v4
4. Create `UserPreferenceRepository.kt` in `data/repository/`
5. Wire `UserPreferenceRepository` in `BioGraphApplication.kt`

**Verify**: App compiles, database migrates without crash on launch.

### Step 2 — ViewModel Sort Logic

1. Add `InsightSortMode` enum to `InsightViewModel.kt` (or a shared file)
2. Add `StrengthTier` enum
3. Add `GroupedInsight` and `DataTypeInsightGroup` data classes
4. Add `InsightSortState` sealed interface
5. In `InsightViewModel`: load persisted sort mode on init, expose `sortMode` and `sortState` StateFlows
6. Add `setSortMode(mode)` method that updates state and persists to Room

**Verify**: ViewModel unit test or log output confirms sort modes produce correct ordering/grouping.

### Step 3 — Strength Badge Composable

1. Create `StrengthBadge.kt` in `ui/components/` — small label + colour accent
2. Add semantic colour selection logic (maps tier to `MaterialTheme.colorScheme` slot)
3. Add `@Preview`

**Verify**: Preview renders correctly for Strong and Moderate tiers.

### Step 4 — Update InsightCard

1. Modify `InsightCard.kt` to accept optional `strengthTier` and `alsoInDataType` parameters
2. Display `StrengthBadge` in the card
3. Display "Also in: [type]" tag when provided

**Verify**: Preview renders cards with badge and cross-reference tag.

### Step 5 — Sort View Composables

1. Create `InsightsByStrength.kt` — flat `LazyColumn` of insight cards
2. Create `InsightsByDataType.kt` — grouped `LazyColumn` with collapsible headers using `AnimatedVisibility`
3. Add `@Preview` for both

**Verify**: Previews render correct layouts.

### Step 6 — Update InsightsPanel

1. Add `SingleChoiceSegmentedButtonRow` below the `PrimaryTabRow` in `InsightsPanel.kt`
2. Wire toggle to `InsightViewModel.setSortMode()`
3. Use `AnimatedContent` to transition between `InsightsByStrength` and `InsightsByDataType`
4. Hide toggle when state is not `Success`

**Verify**: Full flow in emulator — toggle appears, switches sort modes, animates transitions, preference persists across app restart.

## Key Files

| File | Action | Purpose |
|------|--------|---------|
| `data/local/UserPreferenceEntity.kt` | Create | Key/value preference storage |
| `data/local/UserPreferenceDao.kt` | Create | Read/write preference |
| `data/local/AppDatabase.kt` | Modify | Add entity, DAO, migration |
| `data/repository/UserPreferenceRepository.kt` | Create | Repository wrapper |
| `BioGraphApplication.kt` | Modify | Wire repository |
| `viewmodel/InsightViewModel.kt` | Modify | Sort mode state, grouping logic |
| `ui/components/StrengthBadge.kt` | Create | Tier label composable |
| `ui/components/InsightCard.kt` | Modify | Add badge + cross-ref tag |
| `ui/components/InsightsByStrength.kt` | Create | Flat sorted list |
| `ui/components/InsightsByDataType.kt` | Create | Grouped collapsible list |
| `ui/components/InsightsPanel.kt` | Modify | Add sort toggle, delegate to views |
| `ui/theme/Color.kt` | Modify | Add strength tier colour tokens |
