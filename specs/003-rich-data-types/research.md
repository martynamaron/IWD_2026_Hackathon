# Research: Rich Data Input Types

**Date**: 2026-03-12 | **Feature Branch**: `003-rich-data-types`

## 1. Room Auto-Migration (v1 → v2)

**Decision**: Use `@AutoMigration(from = 1, to = 2)` with a spec class for cases needing annotation hints (adding a default-value column).

**Rationale**: Room 2.4+ auto-migration handles the following without handwritten SQL:
- Adding a nullable column to an existing table (`scaleValue INTEGER` — nullable, default null).
- Adding a non-null column with a `DEFAULT` value (`inputType TEXT NOT NULL DEFAULT 'TOGGLE'`). This requires a `@RenameColumn` / no annotation since it is a plain additive change — Room generates the correct ALTER TABLE statement.
- Creating brand-new tables (`multiple_choice_options`, `multi_choice_selections`).

**Constraints checked**:
- Room cannot auto-migrate *removals* of columns, renames without hints, or moves between tables — none of those apply here.
- The existing `fallbackToDestructiveMigration(dropAllTables = true)` in `AppDatabase` must be kept as a **last-resort fallback only** (the auto-migration path takes priority). Destructive fallback is acceptable for a prototype where demo data can be re-seeded.
- `exportSchema = true` is already set — required for auto-migration to read the schema hash.

**Alternatives considered**: Manual `Migration(1, 2)` with raw SQL — rejected because auto-migration reduces boilerplate and is safer for straightforward additive changes.

---

## 2. Discriminated Union Storage in Room — Scale vs MultiChoice vs Toggle

**Decision**: Extend `DailyEntryEntity` with a nullable `scaleValue: Int?` column (for Scale types) and add a separate `MultiChoiceSelectionEntity` table (for MultipleChoice types).

**Rationale**:
- Toggle uses the existing presence-based model: row exists ↔ active; `scaleValue = null`. No change.
- Scale: a row with `scaleValue ∈ {0..5}` = a recorded score. No row = unrecorded (null state). This cleanly distinguishes 0 from unrecorded.
- MultipleChoice requires **multiple rows per (date, dataTypeId)** — one per selected option. The existing `UNIQUE(date, dataTypeId)` index on `daily_entries` prevents this, so a separate junction table is the correct model.
- Keeping Scale in `daily_entries` minimises schema churn: existing Toggle queries are unchanged; calendar-indicator queries add only one extra condition (`scaleValue IS NOT NULL`).

**Alternatives considered**:
- Separate tables per type (ToggleEntryEntity, ScaleEntryEntity, MultiChoiceEntryEntity) — rejected as over-engineering for a 2-type expansion on a prototype.
- JSON blob in a single value column — rejected because it defeats Room's type-safety and query capability.
- Adding optionId to DailyEntryEntity and relaxing the unique index — rejected because it mixes semantics and breaks existing Toggle/Scale uniqueness invariants.

---

## 3. Compose Discrete Scale Step Selector

**Decision**: Implement as a `Row` of `FilledTonalButton` / `OutlinedButton` components (0–5), with `animate*AsState` on background/border color for smooth state transitions.

**Rationale**:
- Material 3 has no out-of-the-box "step selector" component; a `Row` of 6 buttons is idiomatic Compose.
- `FilterChip` was considered but its toggle semantics imply multi-select — misleading for a single-select scale.
- `Slider` (discrete) was rejected: the spec explicitly mandates "discrete tappable steps, no slider".
- Minimum touch target of 48dp is satisfied by Compose's default button minimum touch area.
- Selected state: filled / `MaterialTheme.colorScheme.primary` background; unselected: outlined / surface. The transition between states uses `animateColorAsState(tween(200ms))` per the constitution's motion requirements.

**Alternatives considered**: `Slider(steps = 5)` — explicitly excluded by spec. Custom `Canvas`-drawn step row — rejected as "custom-drawn UI" violation of Principle I.

---

## 4. Material 3 Emoji Chip Row (MultiChoice)

**Decision**: Use `FilterChip` from Material 3 (`androidx.compose.material3`) for each option in a horizontally-scrollable `LazyRow`.

**Rationale**:
- `FilterChip` natively models toggled selection state (`selected: Boolean`) and provides ripple, elevation, and color transitions out of the box — satisfying Principle I and Principle VI simultaneously.
- Multi-select is supported by independently controlling `selected` state per chip — matches FR-007 (individual option deselection).
- Horizontally-scrollable `LazyRow` handles ≤10 chips cleanly without overflow.
- `FilterChip` label supports emoji prefix (`"🏃 Running"` or `leadingIcon` lambda with `Text`).
- No new dependency required — `material3` is already declared in the BOM.

**Alternatives considered**: `InputChip` — designed for user-entered tags with removal; semantically wrong here. Custom pill buttons — rejected because `FilterChip` already satisfies all requirements.

---

## 5. Multiple Choice Option Editor (Create/Edit Screen)

**Decision**: Inline column of editable rows in the data type create/edit dialog — each row contains an emoji picker trigger + a `TextField` for the label. "Add option" button appends a new empty row; swipe-to-dismiss or a remove icon deletes a row.

**Rationale**:
- Constitution Principle I requires Material 3 components: `OutlinedTextField` for label, `IconButton` for the remove action.
- The existing `EmojiPicker.kt` component can be reused for option emoji selection, avoiding duplication.
- `AnimatedVisibility` wraps each row's appearance/disappearance per Principle VI.
- Reordering (drag-to-reorder) is explicitly out of scope — `sortOrder` field in the options table will use insertion order.

---

## 6. Input Type Locking After First Change

**Decision**: Store `inputType` as a `String` column on `DataTypeEntity`. Once a data type's `inputType` is changed from `'TOGGLE'` to `'SCALE'` or `'MULTIPLE_CHOICE'`, the edit screen reads this flag and renders the input type selector as `enabled = false` (or hides it).

**Rationale**:
- Spec FR-008: "once a data type has been changed from Toggle, its input type MUST be read-only."
- No additional `isLocked: Boolean` column needed — `inputType != TOGGLE` is the lock condition.
- UI: `InputTypeSelector` composable accepts `locked: Boolean` param; when true, renders a static text label instead of the radio group.

---

## 7. Calendar Indicator Query Update

**Decision**: Update `getDatesWithEntriesFlow` to UNION results from `daily_entries` and `multi_choice_selections` tables for the given date range.

**Rationale**:
- Toggle and Scale entries are in `daily_entries` (presence of row = recorded value).
- MultipleChoice selections are in `multi_choice_selections` — a day with at least one selection must show the indicator.
- A single UNION query returns DISTINCT dates from both tables cleanly.
- Alternative of mirroring MultiChoice activity into `daily_entries` was considered but rejected as redundant state maintenance.

**Query**:
```sql
SELECT DISTINCT date FROM daily_entries WHERE date BETWEEN :startDate AND :endDate
UNION
SELECT DISTINCT date FROM multi_choice_selections WHERE date BETWEEN :startDate AND :endDate
```

---

## 8. Data Migration — Type Change (Application-Level)

**Decision**: Implement a `migrateDataTypeInputType(dataTypeId, newInputType, options)` repository method that:
1. Counts affected `daily_entries` rows (and `multi_choice_selections` rows) for the given `dataTypeId`.
2. Returns the count to the ViewModel so the UI can show the confirmation dialog with the exact number.
3. On confirmation: deletes all `daily_entries` for that `dataTypeId` + all `multi_choice_selections` for that `dataTypeId`, inserts new `multiple_choice_options` if switching to MultipleChoice, and updates `DataTypeEntity.inputType`.

**Rationale**: This is application-level migration (user-initiated type change), not a DB schema migration. The logic belongs in the repository layer per Principle III.

---

## 9. Onboarding — Mood (Scale) and Exercise (MultiChoice)

**Decision**: Update `OnboardingSuggestion` to carry an `inputType: InputType` field and an optional `options: List<OptionSuggestion>`. Seed logic in `AppDatabase.Callback.onCreate` creates the matching entities and options rows when inserting Mood and Exercise.

**Rationale**: Spec FR-019 requires fresh-install users to get the richer types immediately without a retrofit step.

**Updated defaults**:
```kotlin
OnboardingSuggestion("😊", "Mood", inputType = InputType.SCALE)
OnboardingSuggestion("🏃", "Exercise", inputType = InputType.MULTIPLE_CHOICE, options = listOf(
    OptionSuggestion("🏃", "Running"),
    OptionSuggestion("🏊", "Swimming"),
    OptionSuggestion("🎾", "Tennis"),
    OptionSuggestion("💃", "Dancing")
))
```

---

## 10. No New External Dependencies Required

All needed capabilities are already in the dependency graph:
| Capability | Library | Already Present? |
|---|---|---|
| Discrete step selector | Compose `Button` + Material 3 colors | ✅ |
| Multi-select chips | `material3.FilterChip` | ✅ |
| Option editor text fields | `material3.OutlinedTextField` | ✅ |
| State transition animations | `compose.animation.animateColorAsState` | ✅ |
| JSON (if needed for option storage) | `kotlinx-serialization-json` | ✅ (already added) |
| Room schema migration | `room 2.7.0` auto-migration | ✅ |

**Conclusion**: Zero new dependencies required for this feature.
