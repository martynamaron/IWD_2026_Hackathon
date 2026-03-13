# Tasks: Insight Sorting

**Input**: Design documents from `/specs/006-insight-sorting/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, quickstart.md

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Room schema additions and preference persistence layer

- [ ] T001 [P] Create `UserPreferenceEntity` data class with `@Entity` annotation in `app/src/main/java/com/martynamaron/biograph/data/local/UserPreferenceEntity.kt`
- [ ] T002 [P] Create `UserPreferenceDao` interface with `getValue()` and `upsert()` methods in `app/src/main/java/com/martynamaron/biograph/data/local/UserPreferenceDao.kt`
- [ ] T003 Register `UserPreferenceEntity` in the `@Database` entities array, add `UserPreferenceDao` abstract function, and add `AutoMigration(from = 3, to = 4)` in `app/src/main/java/com/martynamaron/biograph/data/local/AppDatabase.kt`
- [ ] T004 Create `UserPreferenceRepository` with `getSortMode()` and `setSortMode()` methods in `app/src/main/java/com/martynamaron/biograph/data/repository/UserPreferenceRepository.kt`
- [ ] T005 Wire `UserPreferenceRepository` as a lazy property in the dependency container in `app/src/main/java/com/martynamaron/biograph/BioGraphApplication.kt`

**Checkpoint**: App compiles, database auto-migrates to v4 without crash on launch. Sort mode preference can be read/written.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Presentation models and ViewModel sort/grouping logic that all UI stories depend on

**⚠️ CRITICAL**: No UI work can begin until this phase is complete

- [ ] T006 [P] Add `InsightSortMode` enum (`BY_STRENGTH`, `BY_DATA_TYPE`) and `StrengthTier` enum (`STRONG`, `MODERATE`, `MILD`) in `app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt`
- [ ] T007 [P] Add `GroupedInsight` data class (wrapping `InsightEntity` + nullable `alsoInDataTypeName`) and `DataTypeInsightGroup` data class in `app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt`
- [ ] T008 Add `InsightSortState` sealed interface with `ByStrength` and `ByDataType` variants in `app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt`
- [ ] T009 Add `sortMode: StateFlow<InsightSortMode>` and `sortState: StateFlow<InsightSortState>` to `InsightViewModel`, load persisted sort mode on init from `UserPreferenceRepository`, and implement `setSortMode()` method that persists to Room in `app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt`
- [ ] T010 Implement grouping logic in `InsightViewModel`: build `DataTypeInsightGroup` list from insights + data types, compute "Also in" cross-reference tags, sort groups alphabetically, sort insights within each group by `|coefficient|` desc then `computedAt` desc in `app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt`
- [ ] T011 Update `InsightViewModel.Factory` to accept `UserPreferenceRepository` parameter and update the factory call site in `app/src/main/java/com/martynamaron/biograph/ui/screens/calendar/CalendarScreen.kt`

**Checkpoint**: ViewModel correctly produces both sort states. Changing sort mode recomputes derived state and persists preference.

---

## Phase 3: User Story 1 — Sorting Insights by Correlation Strength (Priority: P1) 🎯 MVP

**Goal**: Default "By Strength" flat list with strongest correlations first

**Independent Test**: Open insights panel with mock data — insights appear in descending strength order

### Implementation for User Story 1

- [ ] T012 [P] [US1] Add strength tier semantic colour tokens (`StrengthStrong`, `StrengthModerate`, `StrengthMild`) in `app/src/main/java/com/martynamaron/biograph/ui/theme/Color.kt` and map them in the `lightColorScheme()` in `app/src/main/java/com/martynamaron/biograph/ui/theme/Theme.kt`
- [ ] T013 [P] [US1] Create `StrengthBadge` composable that renders the tier label with the corresponding semantic colour accent, and add `@Preview` for Strong and Moderate tiers in `app/src/main/java/com/martynamaron/biograph/ui/components/StrengthBadge.kt`
- [ ] T014 [US1] Update `InsightCard` to accept optional `strengthTier: StrengthTier?` parameter, display `StrengthBadge` inside the card when provided, and update `@Preview` in `app/src/main/java/com/martynamaron/biograph/ui/components/InsightCard.kt`
- [ ] T015 [US1] Create `InsightsByStrength` composable — a `Column` of `InsightCard` items receiving insights from `InsightSortState.ByStrength`, computing `StrengthTier` from each insight's coefficient, and add `@Preview` in `app/src/main/java/com/martynamaron/biograph/ui/components/InsightsByStrength.kt`

**Checkpoint**: "By Strength" view renders a flat list of insight cards sorted by correlation strength, each showing a strength badge. Independently functional.

---

## Phase 4: User Story 2 — Sorting Insights by Data Type (Priority: P1)

**Goal**: Grouped list with collapsible data-type headers, cross-reference tags, alphabetical group ordering

**Independent Test**: Switch to "By Data Type" — insights appear grouped under correct headers with counts, collapse/expand works, "Also in" tags visible

### Implementation for User Story 2

- [ ] T016 [US2] Update `InsightCard` to accept optional `alsoInDataType: String?` parameter and render a subtle "Also in: [type name]" tag below the insight text when non-null, and update `@Preview` in `app/src/main/java/com/martynamaron/biograph/ui/components/InsightCard.kt`
- [ ] T017 [US2] Create `InsightsByDataType` composable — a `Column` with collapsible group sections: each section has a tappable header row displaying data type name (emoji + description), insight count, and a chevron affordance; group body uses `AnimatedVisibility` with `expandVertically`/`shrinkVertically`; groups start expanded; each insight renders via `InsightCard` with `StrengthTier` and `alsoInDataType`; add `@Preview` in `app/src/main/java/com/martynamaron/biograph/ui/components/InsightsByDataType.kt`

**Checkpoint**: "By Data Type" view renders grouped insights with collapsible headers, correct counts, cross-reference tags, and correct within-group sort order. Independently functional.

---

## Phase 5: User Story 3 — Switching Between Sort Modes (Priority: P1)

**Goal**: Segmented toggle below time-period tabs, animated transitions, persisted preference

**Independent Test**: Toggle between modes — layout transitions smoothly, preference persists across app restart

### Implementation for User Story 3

- [ ] T018 [US3] Update `InsightsPanel` to accept `sortMode: InsightSortMode` and `onSortModeSelected: (InsightSortMode) -> Unit` parameters, add a `SingleChoiceSegmentedButtonRow` with two `SegmentedButton` options ("By Strength", "By Data Type") on a row below the `PrimaryTabRow`, hide the sort toggle when state is not `Success` in `app/src/main/java/com/martynamaron/biograph/ui/components/InsightsPanel.kt`
- [ ] T019 [US3] Replace the inline insight list inside `InsightsPanel`'s `Success` branch with `AnimatedContent` that switches between `InsightsByStrength` and `InsightsByDataType` based on `sortMode`, using fade + slide transition spec (300ms tween with `EaseInOutCubic`) in `app/src/main/java/com/martynamaron/biograph/ui/components/InsightsPanel.kt`
- [ ] T020 [US3] Wire `InsightsPanel` in `CalendarScreen` to pass `sortMode` and `sortState` from `InsightViewModel`, and connect `onSortModeSelected` to `insightViewModel.setSortMode()` in `app/src/main/java/com/martynamaron/biograph/ui/screens/calendar/CalendarScreen.kt`

**Checkpoint**: Full sort toggle flow works end-to-end. Toggle appears only when insights are available, transitions smoothly, preference persists across app restart.

---

## Phase 6: User Story 4 — Understanding Correlation Strength at a Glance (Priority: P2)

**Goal**: Strength badges display correct tier label and colour for all insights in both sort modes

**Independent Test**: Generate insights with varying strengths — each card shows correct "Strong" or "Moderate" badge with correct colour accent

### Implementation for User Story 4

- [ ] T021 [US4] Verify `StrengthBadge` renders correctly in both `InsightsByStrength` and `InsightsByDataType` views, ensure badge is consistently positioned on the card, and add a `@Preview` for `InsightCard` showing both tiers side by side in `app/src/main/java/com/martynamaron/biograph/ui/components/InsightCard.kt`

**Checkpoint**: All insight cards in both sort modes consistently display the correct strength tier label and colour accent.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Edge cases, animation polish, and validation

- [ ] T022 Ensure the sort toggle correctly hides during Loading, InsufficientData, Error, and Hidden states — verify with `AnimatedContent` that the toggle row animates out/in with the rest of the panel in `app/src/main/java/com/martynamaron/biograph/ui/components/InsightsPanel.kt`
- [ ] T023 Handle edge case: when a data type is deleted (cascade), verify that the "By Data Type" groups rebuild correctly, empty groups are removed, and the panel does not crash in `app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt`
- [ ] T024 Handle edge case: when only one data type has insights, verify the "By Data Type" view shows a single group with the toggle still functional in `app/src/main/java/com/martynamaron/biograph/ui/components/InsightsByDataType.kt`
- [ ] T025 Run quickstart.md validation — verify full end-to-end flow per the 6-step implementation order in `specs/006-insight-sorting/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 — delivers MVP "By Strength" view
- **US2 (Phase 4)**: Depends on Phase 2 — can run in parallel with Phase 3
- **US3 (Phase 5)**: Depends on Phases 3 + 4 — wires both views together via toggle
- **US4 (Phase 6)**: Depends on Phases 3 + 4 — verification pass for visual badges
- **Polish (Phase 7)**: Depends on all user story phases

### User Story Dependencies

- **US1 (P1)**: After Foundational → independent (flat list + strength badge)
- **US2 (P1)**: After Foundational → independent (grouped list + cross-ref)
- **US3 (P1)**: After US1 + US2 → integrates both views via toggle
- **US4 (P2)**: After US1 + US2 → verification of badge consistency

### Within Each User Story

- Colour tokens before badge composable (US1)
- Badge composable before card update (US1)
- Card update before list composable (US1, US2)
- List composables before panel integration (US3)

### Parallel Opportunities

**Phase 1** (can run in parallel):
- T001 (entity) ∥ T002 (DAO) — different files, no dependencies

**Phase 2** (can run in parallel):
- T006 (enums) ∥ T007 (data classes) — independent type definitions

**Phase 3 + Phase 4** (can run in parallel across stories):
- US1 (T012–T015) ∥ US2 (T016–T017) — different files, different composables

**Phase 3 internal** (can run in parallel):
- T012 (colour tokens) ∥ T013 (badge composable) — different files

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (Room entity + migration)
2. Complete Phase 2: Foundational (ViewModel sort logic)
3. Complete Phase 3: US1 — "By Strength" view with badges
4. **STOP and VALIDATE**: Insights display in strength order with badges
5. Demo-ready with default sort at this point

### Incremental Delivery

1. Setup + Foundational → Preference persistence + sort logic ready
2. Add US1 → Flat strength-sorted list with badges (MVP!)
3. Add US2 → Grouped data-type list with collapsible headers
4. Add US3 → Toggle between both views, animated transitions, persisted
5. Add US4 → Verify badge consistency in both views
6. Polish → Edge cases + validation

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story
- No tests generated — testing not mandated per constitution Principle IV
- Total: 25 tasks across 7 phases
- App uses Room schema v3 → this feature bumps to v4 via auto-migration
- All new composables MUST include `@Preview` per constitution Principle II
