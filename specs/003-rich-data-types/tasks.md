# Tasks: Rich Data Input Types

**Branch**: `003-rich-data-types` | **Date**: 2026-03-12  
**Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

---

## Phase 1: Setup — DB Schema v2

> Prerequisite for all user stories. No story label. Adds the `InputType` enum, new columns on existing entities, two new Room entities with DAOs, and the `@AutoMigration(1→2)` configuration.

**Goal**: App compiles at DB version 2 and auto-migrates cleanly from version 1. No UI changes yet.

**Independent test**: Build succeeds with `./gradlew assembleDebug`. Existing unit tests (if any) pass. A fresh install shows no crash on DB creation.

- [ ] T001 Create `InputType` enum in `data/InputType.kt` with values `TOGGLE`, `SCALE`, `MULTIPLE_CHOICE`
- [ ] T002 [P] Add `inputType: String = InputType.TOGGLE.name` field to `DataTypeEntity` in `data/local/DataTypeEntity.kt`
- [ ] T003 [P] Add nullable `scaleValue: Int? = null` field to `DailyEntryEntity` in `data/local/DailyEntryEntity.kt`
- [ ] T004 [P] Create `MultipleChoiceOptionEntity` in `data/local/MultipleChoiceOptionEntity.kt` with fields `id`, `dataTypeId` (FK→data_types CASCADE), `emoji`, `label`, `sortOrder`
- [ ] T005 [P] Create `MultiChoiceSelectionEntity` in `data/local/MultiChoiceSelectionEntity.kt` with fields `id`, `date`, `dataTypeId` (FK→data_types CASCADE), `optionId` (FK→multiple_choice_options CASCADE); unique index on `(date, dataTypeId, optionId)`
- [ ] T006 [P] Create `MultipleChoiceOptionDao` in `data/local/MultipleChoiceOptionDao.kt` with `getOptionsForDataTypeFlow`, `getOptionsForDataType`, `insertAll`, `delete`, `deleteAllForDataType` per data-model.md
- [ ] T007 [P] Create `MultiChoiceSelectionDao` in `data/local/MultiChoiceSelectionDao.kt` with `getSelectionsFlow`, `getDatesWithSelectionsFlow`, `countSelectionsForDataType`, `insertAll`, `deleteAllForDateAndType`, `deleteAllForDataType` per data-model.md
- [ ] T008 [P] Add `getById(id: Long)` and `getNonToggleTypes()` queries to `DataTypeDao` in `data/local/DataTypeDao.kt`
- [ ] T009 [P] Add `countEntriesForDataType(dataTypeId: Long): Int` suspend query to `DailyEntryDao` in `data/local/DailyEntryDao.kt`
- [ ] T010 Update `AppDatabase` in `data/local/AppDatabase.kt`: bump to `version = 2`, add `MultipleChoiceOptionEntity` and `MultiChoiceSelectionEntity` to entities list, add `autoMigrations = [AutoMigration(from = 1, to = 2)]`, expose `multipleChoiceOptionDao()` and `multiChoiceSelectionDao()` abstract methods

---

## Phase 2: Foundational — Data Layer & Shared UI

> Blocks all user story phases. Builds the repositories, extends ViewModels with new state shapes, adds the UNION calendar query, and creates the `InputTypeSelector` composable that drives all three creation/edit flows.

**Goal**: Data layer fully capable of reading/writing Scale and MultipleChoice values. `InputTypeSelector` composable available for use. Calendar indicator ready for non-Toggle entries.

**Independent test**: `InputTypeSelector` `@Preview` renders three radio options. `getDatesWithAnyEntryFlow` can be called from `CalendarViewModel` without compile errors.

- [ ] T011 Create `MultipleChoiceRepository` in `data/repository/MultipleChoiceRepository.kt` wrapping `MultipleChoiceOptionDao` and `MultiChoiceSelectionDao`; expose `getOptionsFlow`, `getOptions`, `saveOptions`, `getSelections`, `saveSelections`, `countSelections`, `deleteAllSelectionsForType`
- [ ] T012 [P] Update `DailyEntryRepository` in `data/repository/DailyEntryRepository.kt`: add scale-aware `saveEntries(date, toggleIds, scaleValues: Map<Long,Int>)` overload that upserts `DailyEntryEntity` with `scaleValue` for Scale types and skips MultipleChoice types
- [ ] T013 [P] Add `getDatesWithAnyEntryFlow(startDate, endDate)` UNION query to `DailyEntryDao` (`daily_entries UNION multi_choice_selections`) per data-model.md
- [ ] T014 [P] Update `CalendarViewModel` in `viewmodel/CalendarViewModel.kt` to call `getDatesWithAnyEntryFlow` instead of `getDatesWithEntriesFlow` for the calendar indicator `StateFlow`
- [ ] T015 [P] Create `InputTypeSelector` composable in `ui/components/InputTypeSelector.kt`: three-option radio group (Toggle / Scale / Multiple Choice); accepts `selected: InputType`, `onSelect: (InputType) -> Unit`, `locked: Boolean`; when `locked = true` shows a static `Text` label with no radio buttons; include `@Preview`

---

## Phase 3: User Story 1 — Create a Scale Data Type

**User Story**: As a user, I want to create a new data type tracked on a 0–5 numeric scale, so that I can capture nuanced daily feelings rather than a simple yes/no. *(P1)*

**Goal**: User can create a Scale data type, open a day panel, tap a step value, save, and see it persist on revisit. Value of 0 is visually distinct from unrecorded (null).

**Independent test**: Create a new Scale data type → open day panel → tap step "3" → Save → reopen day → step 3 is highlighted. Tap step "0" → Save → reopen → step 0 is highlighted (not blank). Close without selecting → reopen → no step is highlighted.

- [ ] T016 [US1] Create `ScaleStepSelector` composable in `ui/components/ScaleStepSelector.kt`: `Row` of 6 `FilledTonalButton`s labelled 0–5; `selectedValue: Int?` param; selected step uses `primary` color via `animateColorAsState(tween(200))`; unselected uses `surface` outlined style; tapping the already-selected step deselects (sets null); include `@Preview` with selected and null states
- [ ] T017 [US1] Update `DataTypeEditDialog` in `ui/screens/datatype/DataTypeEditDialog.kt`: embed `InputTypeSelector` (T015) in the create form; when Scale is selected, hide any maximum-value input (fixed at 5 per FR-002); wire `inputType` field to ViewModel state
- [ ] T018 [US1] Extend `CalendarViewModel` state to track `scaleValues: Map<Long, Int?>` (dataTypeId → selected value) for the currently-open day; add `setScaleValue(dataTypeId, value)` and `clearScaleValue(dataTypeId)` methods
- [ ] T019 [US1] Update `DayPanel` in `ui/screens/calendar/DayPanel.kt`: for data types with `inputType == SCALE`, render `ScaleStepSelector` (T016) instead of the existing `Switch`; pass `scaleValues[dataType.id]` as `selectedValue`; forward tap events to `CalendarViewModel`
- [ ] T020 [US1] Wire Scale save/load in `DayPanel` + `CalendarViewModel`: on day open, load existing `DailyEntryEntity.scaleValue` from `DailyEntryRepository` into `scaleValues` map; on "Save", call the scale-aware `saveEntries` (T012) to upsert or delete rows per Scale data type

---

## Phase 4: User Story 2 — Create a Multiple Choice Data Type

**User Story**: As a user, I want to create a data type where I can select one or more emoji-labelled options on a given day, so that I can log richer categorical data including multiple activities. *(P1)*

**Goal**: User can create a Multiple Choice data type with ≥2 options, open a day panel, tap multiple chips, save, and see all selections persist on revisit.

**Independent test**: Create a Multiple Choice data type with options 🏃 Running and 🏊 Swimming → open day panel → tap both chips → Save → reopen day → both chips highlighted. Tap 🏃 again → deselects; 🏊 remains. Save → reopen → only 🏊 highlighted.

- [ ] T021 [P] [US2] Create `MultiChoiceOptionEditor` composable in `ui/components/MultiChoiceOptionEditor.kt`: scrollable column of option rows; each row has an emoji-picker trigger (`EmojiPicker` reuse) + `OutlinedTextField` for the label (max 30 chars) + `IconButton` remove; "Add option" button appended at bottom; `AnimatedVisibility` wraps each row's appearance/disappearance; `enabled = false` on "Add option" when 10 options exist; include `@Preview`
- [ ] T022 [P] [US2] Create `MultiChoiceChipRow` composable in `ui/components/MultiChoiceChipRow.kt`: horizontal `LazyRow` of `FilterChip` items; each chip label = `"${option.emoji} ${option.label}"`; `selected = optionId in selectedIds`; tap calls `onToggle(optionId)`; include `@Preview` with mixed selected/unselected state
- [ ] T023 [US2] Update `DataTypeEditDialog` in `ui/screens/datatype/DataTypeEditDialog.kt`: when Multiple Choice is selected in `InputTypeSelector`, show `MultiChoiceOptionEditor` (T021) below the type selector; wire the option list to `DataTypeViewModel` state; validation runs in ViewModel before allowing save
- [ ] T024 [US2] Extend `CalendarViewModel` state: add `multiChoiceSelections: Map<Long, Set<Long>>` (dataTypeId → set of selected optionIds); add `toggleMultiChoiceOption(dataTypeId, optionId)` that adds/removes from the set
- [ ] T025 [US2] Update `DayPanel` in `ui/screens/calendar/DayPanel.kt`: for data types with `inputType == MULTIPLE_CHOICE`, load options from `MultipleChoiceRepository.getOptionsFlow(dataTypeId)` and render `MultiChoiceChipRow` (T022); pass `multiChoiceSelections[dataType.id]` as `selectedIds`
- [ ] T026 [US2] Wire MultipleChoice save/load in `DayPanel` + `CalendarViewModel`: on day open, load existing `MultiChoiceSelectionEntity` rows from `MultipleChoiceRepository.getSelections(date, dataTypeId)` into `multiChoiceSelections` map; on "Save", call `MultipleChoiceRepository.saveSelections(date, dataTypeId, selectedIds)` (delete + re-insert)

---

## Phase 5: User Story 3 — Retrofit Mood to Scale

**User Story**: As an existing user, I want to change the "Mood" data type (currently a Toggle) into a 0–5 scale, so that I can express nuance going forward without losing the data type. *(P2)*

**Goal**: Existing-user Mood toggle can be edited to Scale; confirmation dialog shows affected entry count; historical entries reset to unrecorded; day panel immediately shows scale selector.

**Independent test**: Open Edit on the Mood data type → input type pre-filled as Scale → Save → confirmation dialog shows N affected entries → confirm → Mood rows in `daily_entries` deleted → day panel shows `ScaleStepSelector`.

- [ ] T027 [US3] Add `migrateDataTypeToScale(dataTypeId: Long)` to `DataTypeRepository` in `data/repository/DataTypeRepository.kt`: deletes all `daily_entries` rows for `dataTypeId`, then updates `DataTypeEntity.inputType` to `SCALE`; done in a repository-level transaction
- [ ] T028 [US3] Add migration confirmation state to `DataTypeViewModel` in `viewmodel/DataTypeViewModel.kt`: add `ConfirmMigrationState` sealed class (`Hidden`, `Pending(affectedCount, newInputType)`); before calling migrate, query `DailyEntryRepository.countEntriesForDataType(dataTypeId)` and emit `Pending(count, SCALE)` into the ViewModel state
- [ ] T029 [US3] Add migration `AlertDialog` to `DataTypeEditDialog` in `ui/screens/datatype/DataTypeEditDialog.kt`: shown when `ConfirmMigrationState.Pending`; body text = "This will reset {N} historical entries to unrecorded. This cannot be undone. Continue?"; Cancel dismisses; Confirm calls `DataTypeViewModel.confirmMigration()`
- [ ] T030 [US3] Pre-fill Mood → Scale in `DataTypeEditDialog`: when the data type being edited has `description == "Mood"` and `inputType == TOGGLE`, automatically pre-select Scale in the `InputTypeSelector` (per FR-011); user still must tap Save and confirm the migration dialog

---

## Phase 6: User Story 4 — Retrofit Exercise to Multiple Choice

**User Story**: As an existing user, I want to change "Exercise" (currently a Toggle) into a Multiple Choice type with sport emojis, so that I can log which activity I did each day. *(P2)*

**Goal**: Existing-user Exercise toggle can be edited to Multiple Choice with 4 pre-populated sport options; confirmation dialog resets historical entries; day panel shows chip row.

**Independent test**: Open Edit on Exercise data type → input type pre-filled as Multiple Choice → 4 sport emoji options pre-populated → Save → confirmation → historical entries deleted → day panel shows `MultiChoiceChipRow` with 🏃🏊🎾💃.

- [ ] T031 [US4] Add `migrateDataTypeToMultipleChoice(dataTypeId: Long, options: List<MultipleChoiceOptionEntity>)` to `MultipleChoiceRepository`: delegates `daily_entries` row deletion to `DailyEntryRepository.deleteAllForDataType(dataTypeId)` (preserving repository domain boundary), deletes all `multi_choice_selections` for `dataTypeId`, inserts new options into `multiple_choice_options`, updates `DataTypeEntity.inputType` to `MULTIPLE_CHOICE`; all wrapped in a `withTransaction` call on the database
- [ ] T032 [US4] Pre-fill Exercise → Multiple Choice in `DataTypeEditDialog`: when edited data type has `description == "Exercise"` and `inputType == TOGGLE`, pre-select Multiple Choice in `InputTypeSelector` and pre-populate `MultiChoiceOptionEditor` with 🏃 Running, 🏊 Swimming, 🎾 Tennis, 💃 Dancing (per FR-012); user can remove or add options before saving
- [ ] T033 [US4] Extend migration confirmation flow in `DataTypeViewModel` to handle Multiple Choice: when new type is `MULTIPLE_CHOICE`, emit `Pending(count, MULTIPLE_CHOICE)` counting both `daily_entries` and `multi_choice_selections` rows; `confirmMigration()` delegates to `MultipleChoiceRepository.migrateDataTypeToMultipleChoice`

---

## Phase 7: User Story 5 — Create New Data Type with Any Input Type

**User Story**: As a user, I want to freely choose Toggle, Scale, or Multiple Choice when creating any new data type, so that my future tracking is not limited to yes/no. *(P3)*

**Goal**: "Add data type" flow requires the user to choose an input type. Toggle creation still works identically to before. Scale and Multiple Choice creation generalises beyond the Mood/Exercise examples.

**Independent test**: Create three new data types in one session — one Toggle, one Scale, one Multiple Choice (3 options). Log values for all three on the same day. Reopen the day and confirm all three persist with correct values and correct UI rendering.

- [ ] T034 [US5] Enforce input type selection in "Add data type" flow: update `DataTypeViewModel.validate()` to require `inputType` is explicitly set (no default shown — user must tap one) when `isNewDataType == true`; show inline error if user tries to save without selecting a type (FR-001)
- [ ] T035 [US5] Verify Toggle creation backward-compatibility through the new flow: add `@Preview` to `DataTypeEditDialog` showing the new-type dialog with Toggle selected; confirm the resulting `DataTypeEntity` is saved with `inputType = TOGGLE` and behaves identically to pre-feature Toggle entries in `DayPanel`

---

## Final Phase: Polish & Cross-Cutting Concerns

> Covers onboarding seeding (FR-019), input type locking (FR-008), Calendar indicator correctness, validation completeness, animation polish, and mock data generator update.

- [ ] T036 [P] Update `OnboardingSuggestion` in `data/OnboardingSuggestion.kt`: add `inputType: InputType = InputType.TOGGLE` and `defaultOptions: List<OptionSuggestion> = emptyList()` fields; add `data class OptionSuggestion(val emoji: String, val label: String)`; update `DEFAULT_SUGGESTIONS` so Mood carries `InputType.SCALE` and Exercise carries `InputType.MULTIPLE_CHOICE` with four sport `OptionSuggestion` entries
- [ ] T037 Update `AppDatabase.Callback.onCreate` in `data/local/AppDatabase.kt`: when inserting each seeded data type, check `suggestion.inputType`; for Scale types set `inputType = SCALE` on the entity; for Multiple Choice types set `inputType = MULTIPLE_CHOICE` and insert `MultipleChoiceOptionEntity` rows using `multipleChoiceOptionDao()` via `MultipleChoiceRepository`
- [ ] T038 [P] Implement input type locking in `DataTypeEditDialog` and `DataTypeViewModel`: when editing a data type with `inputType != TOGGLE`, pass `locked = true` to `InputTypeSelector` (renders as static label, no radio buttons); hide the `InputTypeSelector` entirely for Scale/Multiple Choice edit screens per FR-008
- [ ] T039 [P] Add validation completeness in `DataTypeViewModel.validate()`: duplicate option check (`(emoji + label)` pair must be unique within the option list — FR-015); empty label check (FR-017); min 2 options check (FR-016); surface all errors as a `List<ValidationError>` in the ViewModel state; display inline below the option editor in `DataTypeEditDialog`
- [ ] T040 [P] Disable "Add option" button at 10 options in `MultiChoiceOptionEditor`: when `options.size >= 10`, set `enabled = false` on the button and show a `Text` label "Maximum 10 options reached" below it (edge case from spec)
- [ ] T041 [P] Verify calendar indicator correctness: confirm `CalendarViewModel` uses `getDatesWithAnyEntryFlow` (T013/T014); write `@Preview` or local smoke test confirming that a day with only `multi_choice_selections` rows (no `daily_entries` row) shows the indicator dot
- [ ] T042 [P] Update `MockDataGenerator` in `util/MockDataGenerator.kt`: skip generating fake `DailyEntryEntity` rows for data types with `inputType != TOGGLE`; optionally generate random Scale `scaleValue` entries and random `MultiChoiceSelectionEntity` rows to seed the demo with realistic data
- [ ] T043 [P] Animation polish: verify `AnimatedContent` is used in `DataTypeEditDialog` when `InputTypeSelector` switches between types (smooth transition between the Scale variant and MultiChoice option editor); verify `AnimatedVisibility` wraps option row add/remove in `MultiChoiceOptionEditor`; verify `animateColorAsState` is present in `ScaleStepSelector`; verify `ScaleStepSelector` and `MultiChoiceChipRow` use `AnimatedVisibility` or `AnimatedContent` when first rendered in `DayPanel` (constitution Principle VI: list items MUST animate in on first appearance)
- [ ] T044 Wire re-edit of existing Multiple Choice data types in `DataTypeEditDialog` and `DataTypeViewModel`: when `editingDataType.inputType == MULTIPLE_CHOICE`, load its existing `MultipleChoiceOptionEntity` rows from `MultipleChoiceRepository.getOptions(dataTypeId)` into the ViewModel's option-list state and pre-populate `MultiChoiceOptionEditor`; on Save, diff original vs. current options — insert new ones, delete removed ones (with cascade deleting their historical `multi_choice_selections` rows per the edge case in spec); do NOT show the type migration dialog (type is already locked)

---

## Dependencies

```
Phase 1 (T001–T010)
  └── Phase 2 (T011–T015)
        ├── Phase 3 / US1 (T016–T020)
        │     └── Phase 5 / US3 (T027–T030)
        ├── Phase 4 / US2 (T021–T026)
        │     └── Phase 6 / US4 (T031–T033)
        └── Phase 7 / US5 (T034–T035)   [depends on US1 + US2 complete]

Final Phase (T036–T044) — can begin after Phase 2; T037 needs T036; T044 needs T026 (MC data type fully wired)
```

**Within Phase 1**: T002, T003, T004, T005, T008, T009 are all parallel after T001. T006 needs T004; T007 needs T005. T010 needs all of T002–T009.

**Within Phase 2**: T011–T015 are parallel after Phase 1.

**Within US1**: T016 and T018 can start in parallel; T019 needs T016+T018; T017 needs T015+T016; T020 needs T017+T018+T019.

**Within US2**: T021 and T022 can start in parallel; T023 needs T015+T021; T024 parallel; T025 needs T022+T024; T026 needs T023+T024+T025.

---

## Parallel Execution — Per Story

### US1 (Scale Data Type)
```
T016 (ScaleStepSelector)  ──┐
                             ├──→ T019 (DayPanel scale) ──→ T020 (save/load)
T018 (CalendarVM state)   ──┘
T017 (EditDialog scale)   ──────────────────────────────→ T020
```

### US2 (Multiple Choice)
```
T021 (OptionEditor)  ──→ T023 (EditDialog MC) ──┐
T022 (ChipRow)       ──→ T025 (DayPanel MC)   ──┴──→ T026 (save/load)
T024 (CalendarVM MC) ──────────────────────────┘
```

### US3 (Mood retrofit) — sequential by design (migration must be confirmed)
```
T027 → T028 → T029 → T030
```

### US4 (Exercise retrofit)
```
T031 (migrate repo)  ──→ T033 (wire confirm)
T032 (pre-fill UI)  ──→ T033
```

---

## Implementation Strategy

**MVP scope (US1 + US2 only)**:
Complete Phases 1–4. This delivers fully functional Scale and Multiple Choice creation and logging, which validates the entire new data model. US3–US5 add retrofit and polish on a working foundation.

**Suggested delivery order**:
1. Phase 1 → 2 (data layer complete, no UI yet)
2. US1 (Scale end-to-end — visually validates phase 1+2)
3. US2 (Multiple Choice end-to-end — completes the core feature)
4. US3 + US4 in parallel (both are migration flows, share confirmation dialog)
5. US5 (enforce type selector in add flow — generalises what US1+US2 built)
6. Final Phase (onboarding seed, locking, validation, animation, mock data)

---

## Summary

| Phase | Tasks | Count |
|---|---|---|
| Phase 1: Setup | T001–T010 | 10 |
| Phase 2: Foundational | T011–T015 | 5 |
| Phase 3: US1 Scale (P1) | T016–T020 | 5 |
| Phase 4: US2 Multiple Choice (P1) | T021–T026 | 6 |
| Phase 5: US3 Mood Retrofit (P2) | T027–T030 | 4 |
| Phase 6: US4 Exercise Retrofit (P2) | T031–T033 | 3 |
| Phase 7: US5 General Creation (P3) | T034–T035 | 2 |
| Final Phase: Polish | T036–T044 | 9 |
| **Total** | | **44** |

| User Story | Task count | Independent? |
|---|---|---|
| US1 — Scale (P1) | 5 | ✅ Testable after T001–T015 |
| US2 — Multiple Choice (P1) | 6 | ✅ Testable after T001–T015 |
| US3 — Mood retrofit (P2) | 4 | ✅ Testable with existing Mood data type |
| US4 — Exercise retrofit (P2) | 3 | ✅ Testable with existing Exercise data type |
| US5 — General creation (P3) | 2 | ✅ Testable as additive UI enforcement |

**Parallel opportunities identified**: 8 `[P]`-marked tasks across phases.  
**MVP scope**: Phases 1–4 (26 tasks) — delivers full Scale + Multiple Choice creation and logging.
