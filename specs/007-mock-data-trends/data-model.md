# Data Model: Mock Data Generation & Insight Trends

**Feature**: 007-mock-data-trends  
**Date**: 13 March 2026

## Existing Entities (No Changes)

These entities are already defined and require no schema modifications.

### DataTypeEntity
- `id: Long` (PK, auto-generated)
- `emoji: String`
- `description: String`
- `inputType: String` (TOGGLE | SCALE | MULTIPLE_CHOICE)
- `createdAt: Long`

### DailyEntryEntity
- `id: Long` (PK, auto-generated)
- `date: String` (ISO local date, e.g., "2025-12-15")
- `dataTypeId: Long` (FK → DataTypeEntity)
- `scaleValue: Int?` (0–5, nullable, used only for SCALE types)

### MultipleChoiceOptionEntity
- `id: Long` (PK, auto-generated)
- `dataTypeId: Long` (FK → DataTypeEntity)
- `emoji: String`
- `label: String`
- `sortOrder: Int`

### MultiChoiceSelectionEntity
- `id: Long` (PK, auto-generated)
- `date: String` (ISO local date)
- `optionId: Long` (FK → MultipleChoiceOptionEntity)
- `dataTypeId: Long` (FK → DataTypeEntity)

### InsightEntity
- `id: Long` (PK, auto-generated)
- `dataType1Id: Long` (FK → DataTypeEntity)
- `dataType2Id: Long` (FK → DataTypeEntity)
- `optionId: Long?`
- `correlationCoefficient: Double`
- `correlationMethod: String` (PHI | POINT_BISERIAL | PEARSON)
- `insightText: String`
- `sampleSize: Int`
- `computedAt: Long`

## New Data Types (Seeded by MockDataGenerator)

These are DataTypeEntity + MultipleChoiceOptionEntity rows created at generation time.

| Data Type | Emoji | InputType | Options |
|-----------|-------|-----------|---------|
| Health | 🩺 | MULTIPLE_CHOICE | 🤕 headache, 🤢 stomachache, 😵 feeling dizzy, 🤮 vomiting, 💪 muscle aches, 😴 tiredness |
| Medication | 💊 | TOGGLE | — |
| Mood | 😊 | SCALE | — (0–5) |
| Energy Levels | ⚡ | SCALE | — (0–5) |
| Exercise | 🏋️ | MULTIPLE_CHOICE | 🚶 long walk, 🏊 swimming, 💃 dancing, 🎾 tennis |
| Period Bleeding | 🩸 | TOGGLE | — |

## New UI-Only Data Structures (Not Persisted)

### TrendDirection (enum)

A transient classification computed in InsightViewModel, passed to UI composables. Not stored in Room.

```
enum TrendDirection {
    STRENGTHENING,  // |coeff_second_half| - |coeff_first_half| ≥ 0.15
    WEAKENING,      // |coeff_first_half| - |coeff_second_half| ≥ 0.15
    STABLE          // |difference| < 0.15
}
```

**Display mapping**:
- STRENGTHENING → "↑ Strengthening"
- WEAKENING → "↓ Weakening"
- STABLE → "→ Stable"

### InsightWithTrend (data class)

Wraps an InsightEntity with its optional trend for UI display.

```
data class InsightWithTrend(
    val insight: InsightEntity,
    val trend: TrendDirection?   // null for LAST_MONTH period
)
```

### GroupedInsightWithTrend (data class)

Extends GroupedInsight for the By Data Type view.

```
data class GroupedInsightWithTrend(
    val insight: InsightEntity,
    val alsoInDataTypeName: String?,
    val trend: TrendDirection?
)
```

## Relationships

```
DataTypeEntity 1──∞ DailyEntryEntity          (via dataTypeId)
DataTypeEntity 1──∞ MultipleChoiceOptionEntity (via dataTypeId)
DataTypeEntity 1──∞ MultiChoiceSelectionEntity (via dataTypeId)
MultipleChoiceOptionEntity 1──∞ MultiChoiceSelectionEntity (via optionId)
DataTypeEntity 1──∞ InsightEntity              (via dataType1Id, dataType2Id)
InsightEntity  ─── TrendDirection              (computed, not FK)
```

## State Transitions

### Mock Data Generation Flow
```
No Data → User selects time range → Generating → Data Exists
Data Exists → User triggers generation → Clearing → Generating → Data Exists
```

### Trend Computation Flow
```
Insights loaded for period → 
  If LAST_MONTH: trend = null for all insights
  If LAST_3_MONTHS or LAST_YEAR:
    Split dates into halves →
    Run CorrelationEngine on each half →
    Match pairs → Compute Δ → Assign TrendDirection
```

## Validation Rules

- Mood and Energy scale values: 0 ≤ value ≤ 5 (integer)
- Medication and Period Bleeding: present (entry exists) or absent (no entry)
- Health MC selections: 0–6 options per day (at least 1 when health entry exists)
- Exercise MC selections: 0–4 options per day (at least 1 when exercise entry exists)
- Cycle length: 26–30 days
- Bleeding duration: 4–6 days
- Trend threshold: Δ ≥ 0.15 (15 percentage points)
