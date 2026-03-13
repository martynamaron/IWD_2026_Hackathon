# Data Model: Insight Sorting

**Feature**: 006-insight-sorting  
**Date**: 13 March 2026

## Existing Entities (unchanged)

### InsightEntity (Room — `insights` table)

Already stores everything needed for sorting and grouping:

| Field | Type | Purpose for this feature |
|-------|------|--------------------------|
| `id` | Long (PK) | Unique identifier |
| `dataType1Id` | Long (FK → DataTypeEntity) | First data type in the correlation — used for grouping |
| `dataType2Id` | Long (FK → DataTypeEntity) | Second data type in the correlation — used for grouping |
| `correlationCoefficient` | Double | Absolute value used for sort order and strength tier computation |
| `insightText` | String | Displayed on the card |
| `computedAt` | Long | Sub-sort tiebreaker (most recent first) |

**No schema changes needed** on this entity.

### DataTypeEntity (Room — `data_types` table)

Used to resolve data type names for group headers and "Also in" tags:

| Field | Type | Purpose for this feature |
|-------|------|--------------------------|
| `id` | Long (PK) | Join key for insight grouping |
| `emoji` | String | Displayed in group headers |
| `description` | String | Group header label |

**No schema changes needed** on this entity.

---

## New Entity

### UserPreferenceEntity (Room — `user_preferences` table)

A generic key/value store for user preferences. Starts with one key (`insight_sort_mode`) but can accommodate future preferences without migration.

```kotlin
@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val key: String,      // e.g., "insight_sort_mode"
    val value: String                  // e.g., "BY_STRENGTH" or "BY_DATA_TYPE"
)
```

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `key` | String | PK | Preference identifier |
| `value` | String | NOT NULL | Preference value as string |

**Validation rules**:
- `key` must be a non-empty string
- `value` for `insight_sort_mode` must be one of: `"BY_STRENGTH"`, `"BY_DATA_TYPE"`

**Migration**: Room auto-migration v3 → v4 (additive — new table only).

---

## Presentation Models (not persisted)

### InsightSortMode (enum)

```kotlin
enum class InsightSortMode {
    BY_STRENGTH,
    BY_DATA_TYPE
}
```

Represents the current sort mode in the ViewModel. Default: `BY_STRENGTH`.

### StrengthTier (enum)

```kotlin
enum class StrengthTier(val label: String) {
    STRONG("Strong"),     // |coefficient| >= 0.80
    MODERATE("Moderate")  // |coefficient| >= 0.60 (below 0.60 filtered by spec 004's threshold)
}
```

Derived from `InsightEntity.correlationCoefficient`. Not persisted — computed at render time.

Note: The spec defines a "Mild" tier for < 60%, but spec 004's correlation threshold (|ρ| ≥ 0.35 coefficient, mapped to ~60% in insight text) means all displayed insights will be either Strong or Moderate. The tier computation handles the Mild case defensively.

### GroupedInsight (data class)

```kotlin
data class GroupedInsight(
    val insight: InsightEntity,
    val alsoInDataTypeName: String?  // null if insight only relevant to one group context
)
```

Wraps an `InsightEntity` with the cross-reference tag for the "By Data Type" view. Created by the ViewModel during grouping.

### DataTypeInsightGroup (data class)

```kotlin
data class DataTypeInsightGroup(
    val dataTypeName: String,       // e.g., "🏃 Exercise"
    val insightCount: Int,
    val insights: List<GroupedInsight>
)
```

Represents one collapsible section in the "By Data Type" view. Groups are sorted alphabetically by `dataTypeName`.

---

## State Model (ViewModel)

### InsightSortState (sealed interface)

```kotlin
sealed interface InsightSortState {
    data class ByStrength(
        val insights: List<InsightEntity>  // sorted by |coefficient| desc, then computedAt desc
    ) : InsightSortState

    data class ByDataType(
        val groups: List<DataTypeInsightGroup>  // sorted alphabetically by dataTypeName
    ) : InsightSortState
}
```

The `InsightViewModel` exposes:
- `sortMode: StateFlow<InsightSortMode>` — current toggle selection
- `sortState: StateFlow<InsightSortState>` — derived from `InsightPanelState.Success` + `sortMode`

The sort state is recomputed whenever the sort mode changes or new insights arrive. Both derivations operate on the same `List<InsightEntity>` already in memory — no additional DB queries.

---

## Relationships

```
DataTypeEntity (1) ──< InsightEntity (N) via dataType1Id
DataTypeEntity (1) ──< InsightEntity (N) via dataType2Id

UserPreferenceEntity ── standalone (no FK)

InsightEntity ──> GroupedInsight (presentation wrapper, 1:1)
GroupedInsight ──> DataTypeInsightGroup (N insights per group)
```

---

## DAO Changes

### New: UserPreferenceDao

```kotlin
@Dao
interface UserPreferenceDao {
    @Query("SELECT value FROM user_preferences WHERE `key` = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preference: UserPreferenceEntity)
}
```

### Modified: InsightDao

No changes required. Existing `getAllInsights()` returns insights sorted by `abs(correlationCoefficient) DESC`, which is exactly the "By Strength" order. Grouping is done in-memory by the ViewModel.
