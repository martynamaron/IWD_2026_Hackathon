# Implementation Plan: Rich Data Input Types

**Branch**: `003-rich-data-types` | **Date**: 2026-03-12 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-rich-data-types/spec.md`

## Summary

Extend the existing BioGraph personal tracker (spec 001) so that data types support three distinct input modes: **Toggle** (existing binary on/off), **Scale** (discrete integer 0–5), and **Multiple Choice** (multi-select emoji-labelled options). The two most impactful retrofits — Mood → Scale and Exercise → Multiple Choice — are pre-wired in the edit screen. Fresh-install users receive Mood as Scale and Exercise as Multiple Choice directly from onboarding. Implementation requires a Room DB schema bump (v1 → v2 via auto-migration), three new Compose components (scale step selector, emoji chip row, option editor), and expanded ViewModel / repository logic for migration-with-confirmation.

## Technical Context

**Language/Version**: Kotlin 2.0.21 (Compose compiler plugin)
**Primary Dependencies**: Jetpack Compose BOM 2026.03.00, Material 3, Room 2.7.0, Navigation Compose, kotlinx-serialization-json (already present)
**Storage**: Room (SQLite) — on-device only; DB version bumped 1 → 2 via auto-migration
**Testing**: Not mandated (constitution Principle IV); JUnit available in the project
**Target Platform**: Android (minSdk 24, targetSdk 36)
**Project Type**: Mobile app (single-activity Compose)
**Performance Goals**: 60 fps UI; scale step tap response < 100 ms; chip selection < 100 ms
**Constraints**: Offline-only, single-user, no new external dependencies required
**Scale/Scope**: 3 new UI components, ~4 modified screens/dialogs, 4 new DB tables/columns, 1 DB migration

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Design Gate

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Material 3 Design Consistency | ✅ PASS | New components use `FilterChip`, `FilledTonalButton`, `OutlinedTextField` — all M3. Colors via `MaterialTheme.colorScheme`. No hardcoded colors. |
| II. Compose-First UI Architecture | ✅ PASS | All new UI in Jetpack Compose. Stateless composables, state hoisted to ViewModels. New components placed in `ui/components/`. |
| III. Clean Kotlin Structure | ✅ PASS | MVVM maintained: new entities/DAOs in `data/local/`, new repository in `data/repository/`, extended ViewModels in `viewmodel/`. Sealed classes for migration state. |
| IV. Simplicity & Pragmatism | ✅ PASS | Zero new external dependencies. Room auto-migration (no manual SQL). No DI framework. Option storage uses simple entity table — no JSON blobs. |
| V. Local Database | ✅ PASS | Room auto-migration v1→v2. New entities as `@Entity` data classes. New DAOs as `@Dao` interfaces. All access via repository layer. |
| VI. Delight & Motion Design | ✅ PASS | Scale step transitions use `animateColorAsState(tween(200ms))`. Chip selection uses M3 `FilterChip` built-in animation. Option rows use `AnimatedVisibility` on add/remove. |
| UI & Design Standards | ✅ PASS | 48dp touch targets on step buttons and chips. `sp` text. M3 Snackbar for errors. AnimatedContent for input type switching in edit dialog. |

**Pre-design gate result: PASS** — No violations. Proceed to Phase 0.

### Post-Design Re-evaluation (after Phase 1)

| Principle | Status | Post-Design Notes |
|-----------|--------|-------------------|
| I. Material 3 Design Consistency | ✅ PASS | `FilterChip` for multi-select chips; `FilledTonalButton`/outlined for scale steps; `AlertDialog` for migration confirmation. Centralised theme unchanged. |
| II. Compose-First UI Architecture | ✅ PASS | `ScaleStepSelector`, `MultiChoiceChipRow`, `MultiChoiceOptionEditor`, `InputTypeSelector` are all stateless composables with hoisted state. |
| III. Clean Kotlin Structure | ✅ PASS | `MultipleChoiceRepository` owns migration logic. ViewModels emit `ConfirmMigrationEvent` as sealed class. No business logic in composables. |
| IV. Simplicity & Pragmatism | ✅ PASS | No new dependencies. Two new DB tables (not three separate entity supersets). Auto-migration. Calendar query extended with one UNION clause. |
| V. Local Database | ✅ PASS | `@AutoMigration(from = 1, to = 2)`. Four DB objects added/modified (2 entities expanded, 2 new tables). Repository layer maintained. |
| VI. Delight & Motion Design | ✅ PASS | `AnimatedVisibility` on option editor rows. `animateColorAsState` on step buttons. M3 `FilterChip` built-in state animation. |

**Post-design gate result: PASS** — No violations after design review.

## Project Structure

### Documentation (this feature)

```text
specs/003-rich-data-types/
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
│   ├── InputType.kt                                     # NEW — enum TOGGLE/SCALE/MULTIPLE_CHOICE
│   ├── OnboardingSuggestion.kt                          # MODIFIED — + inputType, defaultOptions
│   ├── local/
│   │   ├── AppDatabase.kt                               # MODIFIED — version 2, new DAOs, AutoMigration
│   │   ├── DataTypeEntity.kt                            # MODIFIED — + inputType field
│   │   ├── DailyEntryEntity.kt                          # MODIFIED — + scaleValue field
│   │   ├── MultipleChoiceOptionEntity.kt                # NEW
│   │   ├── MultiChoiceSelectionEntity.kt                # NEW
│   │   ├── DataTypeDao.kt                               # MODIFIED — + getById, getNonToggleTypes
│   │   ├── DailyEntryDao.kt                             # MODIFIED — + countEntriesForDataType, getDatesWithAnyEntryFlow
│   │   ├── MultipleChoiceOptionDao.kt                   # NEW
│   │   └── MultiChoiceSelectionDao.kt                   # NEW
│   └── repository/
│       ├── DataTypeRepository.kt                        # MODIFIED — + migrateToScale (owns daily_entries deletion via DailyEntryRepository)
│       ├── DailyEntryRepository.kt                      # MODIFIED — scale-aware save/load
│       └── MultipleChoiceRepository.kt                  # NEW — option + selection CRUD, migrateToMultipleChoice (delegates DailyEntry deletion to DailyEntryRepository)
├── viewmodel/
│   ├── DataTypeViewModel.kt                             # MODIFIED — inputType state, option list, migration event
│   └── CalendarViewModel.kt                             # MODIFIED — scale + multiChoice day-panel save/load
└── ui/
    ├── components/
    │   ├── ScaleStepSelector.kt                         # NEW — discrete 0–5 step row
    │   ├── MultiChoiceChipRow.kt                        # NEW — LazyRow of FilterChip
    │   ├── MultiChoiceOptionEditor.kt                   # NEW — add/remove option inline editor
    │   └── InputTypeSelector.kt                         # NEW — Toggle/Scale/MultipleChoice radio group
    └── screens/
        ├── datatype/
        │   └── DataTypeEditDialog.kt                    # MODIFIED — InputTypeSelector + option editor + migration confirm dialog
        ├── calendar/
        │   └── DayPanel.kt                              # MODIFIED — conditional scale/multiChoice rendering
        └── onboarding/
            └── OnboardingScreen.kt                      # MODIFIED — pass inputType + options when seeding
```

**Structure Decision**: Single Android module. All new code follows the existing `ui/screens/<feature>/`, `viewmodel/`, `data/` package layout mandated by the constitution. No new modules or build targets are introduced.

## Complexity Tracking

> No constitution violations — this section is intentionally empty.
