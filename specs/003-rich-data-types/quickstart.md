# Quickstart: Rich Data Input Types

**Feature Branch**: `003-rich-data-types` | **Date**: 2026-03-12  
**Builds on**: `001-add-track-data` (must be merged first)

---

## Prerequisites

- Android Studio Meerkat or later
- JDK 17+ (configured in `gradle.properties`)
- Android device or emulator running API 24+
- Spec 001 implementation complete and on `main`

---

## Key Files — What Changes and What Is New

### New files

| File | Purpose |
|---|---|
| `data/local/MultipleChoiceOptionEntity.kt` | Room entity for option definitions |
| `data/local/MultipleChoiceOptionDao.kt` | DAO for option CRUD |
| `data/local/MultiChoiceSelectionEntity.kt` | Room entity for day-level selections |
| `data/local/MultiChoiceSelectionDao.kt` | DAO for selection CRUD |
| `data/InputType.kt` | `enum class InputType { TOGGLE, SCALE, MULTIPLE_CHOICE }` |
| `data/repository/MultipleChoiceRepository.kt` | Wraps option + selection DAOs; owns migration logic |
| `ui/components/ScaleStepSelector.kt` | Row of 6 tappable step buttons (0–5) |
| `ui/components/MultiChoiceChipRow.kt` | Horizontally-scrollable `FilterChip` row |
| `ui/components/MultiChoiceOptionEditor.kt` | Inline add/remove option editor |
| `ui/components/InputTypeSelector.kt` | Radio group Toggle / Scale / Multiple Choice |

### Modified files

| File | Change |
|---|---|
| `data/local/DataTypeEntity.kt` | + `inputType: String = InputType.TOGGLE.name` |
| `data/local/DailyEntryEntity.kt` | + `scaleValue: Int? = null` |
| `data/local/DataTypeDao.kt` | + `getById`, `getNonToggleTypes` queries |
| `data/local/DailyEntryDao.kt` | + `countEntriesForDataType`, `getDatesWithAnyEntryFlow` (UNION query) |
| `data/local/AppDatabase.kt` | Version 1→2, add new entities + DAOs, add `AutoMigration(1, 2)`, remove `fallbackToDestructiveMigration` in production path |
| `data/OnboardingSuggestion.kt` | + `inputType`, `defaultOptions` fields; update Mood + Exercise entries |
| `data/repository/DataTypeRepository.kt` | + `migrateToScale`, `migrateToMultipleChoice` methods |
| `data/repository/DailyEntryRepository.kt` | + Scale-aware save/load logic |
| `viewmodel/DataTypeViewModel.kt` | Extended state for inputType selection, option list editing, migration confirmation |
| `viewmodel/CalendarViewModel.kt` | Handle scale + multiChoice save/load per data type |
| `ui/screens/datatype/DataTypeEditDialog.kt` | + `InputTypeSelector`, conditional `MultiChoiceOptionEditor`, migration confirmation dialog |
| `ui/screens/calendar/DayPanel.kt` | Conditional rendering: `ScaleStepSelector` or `MultiChoiceChipRow` per data type |
| `ui/screens/onboarding/OnboardingScreen.kt` | Pass `inputType` + `defaultOptions` when creating seeded data types |

---

## Room Migration: v1 → v2

Room auto-migration handles all schema changes. The `@AutoMigration(from = 1, to = 2)` annotation is all that is needed; no manual `Migration` object is required.

**What the migration adds:**
1. `data_types.inputType TEXT NOT NULL DEFAULT 'TOGGLE'` — existing rows become Toggle.
2. `daily_entries.scaleValue INTEGER` (nullable) — existing Toggle rows get `NULL`.
3. New table `multiple_choice_options`.
4. New table `multi_choice_selections`.

**Development tip**: If you are iterating locally and have no user data to preserve, you may keep `fallbackToDestructiveMigration(dropAllTables = true)` in the builder. The database will be re-seeded by the `onCreate` callback. Remove it before release.

---

## How "Save" Works Per Input Type

### Toggle (unchanged)
1. Day panel shows a `Switch`.
2. User flips switch → ViewModel tracks active `dataTypeId`s.
3. On "Save": delete all `daily_entries` for the date, re-insert active ones (`scaleValue = null`).

### Scale (new)
1. Day panel shows `ScaleStepSelector(value: Int?, onSelect: (Int) -> Unit)`.
2. User taps a step → ViewModel sets `scaleValue` for that `dataTypeId`.
3. On "Save": upsert a `DailyEntryEntity(date, dataTypeId, scaleValue)` if a value was chosen; delete any existing row if value was cleared (null → unrecorded).

### Multiple Choice (new)
1. Day panel shows `MultiChoiceChipRow(options, selectedIds, onToggle)`.
2. User taps chips → ViewModel maintains a `Set<Long>` of selected `optionId`s per `dataTypeId`.
3. On "Save": delete all `MultiChoiceSelectionEntity` rows for `(date, dataTypeId)`, then insert new ones for selected IDs.

---

## Migration Flow — Changing a Toggle to Scale or MultipleChoice

Triggered when a user edits an existing Toggle data type and selects a new input type.

```
User taps "Save" on edit dialog
     │
     ▼
DataTypeViewModel.saveEdit()
     │
     ├─ [inputType unchanged] → normal DataTypeEntity.update()
     │
     └─ [inputType changed from TOGGLE]
          │
          ▼
     DataTypeRepository.countAffectedEntries(dataTypeId)
          │
          ▼
     Emit ConfirmMigrationEvent(affectedCount)
          │
          ▼
     User sees confirmation dialog:
       "This will reset N historical entries to unrecorded. Continue?"
          │
     [Cancel] ──→ dismiss, no change
          │
     [Confirm]
          │
          ▼
     MultipleChoiceRepository.migrateDataType(
         dataTypeId, newInputType, newOptions
     )
     ├─ DELETE FROM daily_entries WHERE dataTypeId = ?
     ├─ DELETE FROM multi_choice_selections WHERE dataTypeId = ?
     ├─ [if MULTIPLE_CHOICE] INSERT new options into multiple_choice_options
     └─ UPDATE data_types SET inputType = ? WHERE id = ?
```

---

## Calendar Indicator Update

The `CalendarViewModel` must use the new `getDatesWithAnyEntryFlow` query (UNION of `daily_entries` and `multi_choice_selections`) instead of the old `getDatesWithEntriesFlow`. This is a one-line change in the repository call site.

---

## New UI Components — Usage Examples

### ScaleStepSelector

```kotlin
ScaleStepSelector(
    selectedValue = scaleValue,       // Int? — null = unrecorded
    onValueSelected = { v -> vm.setScaleValue(dataTypeId, v) },
    onValueCleared  = { vm.clearScaleValue(dataTypeId) }   // tap selected step again to deselect
)
```

Renders a `Row` of 6 `FilledTonalButton`s (0..5). The selected button uses `MaterialTheme.colorScheme.primary`; others use `MaterialTheme.colorScheme.surface`. Color transitions use `animateColorAsState(tween(200))`.

### MultiChoiceChipRow

```kotlin
MultiChoiceChipRow(
    options     = listOf(MultipleChoiceOptionEntity(...), ...),
    selectedIds = selectedOptionIds,   // Set<Long>
    onToggle    = { id -> vm.toggleOption(dataTypeId, id) }
)
```

Renders a horizontal `LazyRow` of `FilterChip` composables. Each chip shows `"${option.emoji} ${option.label}"` as its label. `selected = optionId in selectedIds`.

### InputTypeSelector

```kotlin
InputTypeSelector(
    selected = currentInputType,         // InputType
    onSelect = { vm.setInputType(it) },
    locked   = existingDataType != null && existingDataType.inputType != InputType.TOGGLE.name
)
```

When `locked = true`, shows a static `Text` label — no radio buttons.

---

## Validation Summary (UI Layer)

| Rule | Where enforced | Error presentation |
|---|---|---|
| Multiple Choice: min 2 options | `DataTypeViewModel.validate()` | Inline error text below option list |
| Multiple Choice: max 10 options | `MultiChoiceOptionEditor` — disables "Add" button at 10 | Button disabled + tooltip text |
| Option label: non-empty | `DataTypeViewModel.validate()` | Inline error text on the empty field |
| Option label: max 30 chars | `OutlinedTextField(maxLines, visualTransformation)` | Character counter + enforced limit |
| Duplicate option: same emoji + label | `DataTypeViewModel.validate()` | Inline error text |
| Scale max is fixed at 5 | No input field shown — spec FR-002 | N/A (field absent) |

---

## Pre-populated Data — Existing Users (Mood / Exercise)

For existing users who already have Mood/Exercise as Toggles: the edit dialog detects these by `description == "Mood"` or `description == "Exercise"` and pre-fills the recommended configuration (Scale for Mood, Multiple Choice with 4 sport options for Exercise) per FR-011, FR-012. The user still confirms explicitly.

---

## Demo Validation Checklist

After running the app on a clean install:

- [ ] Onboarding: Mood selector shows `ScaleStepSelector`; Exercise shows `MultiChoiceChipRow` with 🏃🏊🎾💃 chips.
- [ ] Create new Scale data type → day panel shows discrete 0–5 steps.
- [ ] Select 0 on scale → reopening day shows 0 as active (not blank).
- [ ] Create Multi-Choice type with 2+ options → day panel shows chip row.
- [ ] Select multiple chips → reopening day shows all chips highlighted.
- [ ] Calendar indicator appears on days with a scale score or any selected chip; no indicator on unrecorded days.
- [ ] Edit a Toggle type to Scale → migration confirmation dialog shows affected count → confirm → historical days show as unrecorded.
- [ ] After type change, re-opening edit screen shows input type as read-only.
- [ ] Attempting to save Multi-Choice type with 1 option → inline error, save blocked.
