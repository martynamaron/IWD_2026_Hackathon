# Tasks: Calendar Insights Panel

**Input**: Design documents from `/specs/004-calendar-insights/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, quickstart.md

**Tests**: Not mandated per constitution Principle IV. No test tasks included.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Mobile (Android)**: `BioGraphApp/app/src/main/java/com/martynamaron/biograph/`
- All source paths below are relative to that base

---

## Phase 1: Setup

No setup tasks — this feature builds on the existing BioGraph project structure established by specs 001 and 003. No new external dependencies are required.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add new Room entities, DAOs, and database migration v2→v3 that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T001 [P] Create InsightEntity Room entity with fields for dataType1Id, dataType2Id, optionId (nullable), correlationCoefficient, correlationMethod, insightText, sampleSize, computedAt; include ForeignKey CASCADE DELETE on both dataType FKs and indices per data-model.md in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/InsightEntity.kt
- [X] T002 [P] Create AnalysisMetadataEntity Room entity with single-row pattern (id=0 PK), lastAnalysisTimestamp, and lastDataCount fields per data-model.md in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/AnalysisMetadataEntity.kt
- [X] T003 [P] Create InsightDao interface with getTopInsightsFlow (Flow, sorted by abs coefficient DESC, limit param), getAllInsights (suspend), insertAll, and deleteAll queries per data-model.md in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/InsightDao.kt
- [X] T004 [P] Create AnalysisMetadataDao interface with getMetadata (suspend), getMetadataFlow (Flow), and upsert methods per data-model.md in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/AnalysisMetadataDao.kt
- [X] T005 [P] Add getTotalCount() suspend query (SELECT COUNT) and getAllEntries() suspend query (SELECT * ORDER BY date) to existing DailyEntryDao in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/DailyEntryDao.kt
- [X] T006 [P] Add getTotalCount() suspend query (SELECT COUNT) and getAllSelections() suspend query (SELECT * ORDER BY date) to existing MultiChoiceSelectionDao in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/MultiChoiceSelectionDao.kt
- [X] T007 Update AppDatabase to version 3: add InsightEntity and AnalysisMetadataEntity to entities array, add AutoMigration(from=2, to=3), expose insightDao() and analysisMetadataDao() abstract functions in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/AppDatabase.kt

**Checkpoint**: Database v3 ready with new tables and queries — user story implementation can now begin

---

## Phase 3: User Story 1 — Viewing Insights on the Calendar Screen (Priority: P1) 🎯 MVP

**Goal**: Surface pattern correlations from tracked data as plain-language sentences with emoji in an insights panel below the calendar grid

**Independent Test**: Seed the app with at least 2 weeks of varied daily entries across multiple data types (toggle, scale, multiple choice), then open the Calendar screen and verify that relevant correlation insights appear in the bottom panel sorted by strength

### Implementation for User Story 1

- [X] T008 [P] [US1] Create CorrelationEngine as a pure Kotlin class in data/analysis/ that builds a day-indexed value matrix from daily entries and MC selections, then computes pairwise correlations: Phi coefficient for Toggle↔Toggle, Point-biserial for Toggle→Scale, Pearson for Scale↔Scale, and binary-exploded MC options treated as toggles; filter results to |coefficient| ≥ 0.35 per research.md threshold; return List<CorrelationResult> sorted by |coefficient| DESC in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/analysis/CorrelationEngine.kt
- [X] T009 [P] [US1] Create InsightTextGenerator that converts CorrelationResult data into human-readable templated sentences with emoji interpolation using the 6 template patterns from research.md (Toggle↔Toggle positive/negative, Toggle→Scale positive/negative, Scale↔Scale positive, MC option→Scale); include percentage/frequency in every template per FR-005 in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/analysis/InsightTextGenerator.kt
- [X] T010 [US1] Create InsightRepository wrapping InsightDao and AnalysisMetadataDao with getTopInsightsFlow(), replaceAllInsights(), needsReanalysis() (compares current daily entry + MC selection count vs stored lastDataCount), and recordAnalysisCompleted() per data-model.md in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/repository/InsightRepository.kt
- [X] T011 [US1] Create InsightViewModel with InsightPanelState sealed interface (Loading, Success, InsufficientData, Error, Hidden), expose stateFlow as StateFlow<InsightPanelState>, on init check needsReanalysis — if true run CorrelationEngine on Dispatchers.Default then persist results via InsightRepository, otherwise load cached insights; include companion Factory following existing CalendarViewModel.Factory pattern in BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt
- [X] T012 [P] [US1] Create InsightCard composable displaying a single InsightEntity as a Material 3 Card with insightText; use MaterialTheme.colorScheme for colors and MaterialTheme.typography for text styles; add @Preview annotation in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightCard.kt
- [X] T013 [US1] Create InsightsPanel composable that takes InsightPanelState and renders: Success → scrollable list of InsightCard items; Loading → placeholder text "Analysing patterns…"; InsufficientData → friendly message; Error → error message; Hidden → nothing; add @Preview annotations for each state in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightsPanel.kt
- [X] T014 [US1] Add insightRepository lazy property to BioGraphApplication (constructed from database.insightDao(), database.analysisMetadataDao(), database.dailyEntryDao(), database.multiChoiceSelectionDao()) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/BioGraphApplication.kt
- [X] T015 [US1] Integrate InsightsPanel into CalendarScreen: add InsightViewModel parameter with default factory (same pattern as CalendarViewModel), wrap existing Column content in verticalScroll modifier, add InsightsPanel composable below the AnimatedContent calendar grid, collect insightViewModel.stateFlow as state in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/calendar/CalendarScreen.kt

**Checkpoint**: Opening Calendar with sufficient seeded data shows correlation insights in the bottom panel. Cached insights load from DB on cold restart.

---

## Phase 4: User Story 2 — Delightful Loading State (Priority: P1)

**Goal**: Replace placeholder loading text with a custom animated dots loader that transitions smoothly to insight content

**Independent Test**: Open the Calendar screen with enough data and verify a custom animated loader (3 bouncing dots with "Discovering patterns…" subtext) appears before insights, and that it crossfades to content on completion

### Implementation for User Story 2

- [X] T016 [P] [US2] Create InsightLoader composable with 3 bouncing dots using rememberInfiniteTransition and animateFloat (tween 600ms EaseInOutCubic, 100ms stagger per dot), dots as Box with CircleShape from MaterialTheme.colorScheme, "Discovering patterns…" subtext in bodySmall with animated alpha; add @Preview annotation in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightLoader.kt
- [X] T017 [US2] Update InsightsPanel Loading state to use InsightLoader composable; wrap state rendering in AnimatedContent with fadeIn + fadeOut transition (tween 300ms EaseInOutCubic) for smooth loading → content crossfade per research.md animation specs in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightsPanel.kt

**Checkpoint**: Loading state shows custom bouncing dots animation that smoothly crossfades to reveal insights

---

## Phase 5: User Story 3 — Handling Insufficient Data (Priority: P2)

**Goal**: Show friendly encouragement messages when users don't have enough tracked data for meaningful pattern analysis

**Independent Test**: Open the Calendar screen on a fresh install (no data) and verify the panel is hidden; add data for fewer than 7 days and verify the "keep tracking" message appears; track only one data type and verify the "track more types" message appears

### Implementation for User Story 3

- [X] T018 [P] [US3] Update InsightsPanel InsufficientData state to display a friendly M3-styled message with encouragement text ("Keep tracking! Insights will appear once there's enough data to find patterns.") and Hidden state to render nothing (empty composable); differentiate messages for < 7 days, 7–13 days, and single data type per acceptance scenarios in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightsPanel.kt
- [X] T019 [US3] Add minimum data threshold checking to InsightViewModel: query total data types count (≥ 2 required) and total days with entries (≥ 7 required) before running analysis; emit Hidden when no data types exist, InsufficientData with appropriate message when thresholds not met per FR-008 and FR-009 in BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt

**Checkpoint**: Users with no data see nothing; users with insufficient data see an encouraging message; users with sufficient data still see insights

---

## Phase 6: User Story 4 — Non-Medical and Pattern-Focused Insights (Priority: P2)

**Goal**: Ensure all insights are strictly observational with a persistent non-medical disclaimer visible whenever insights are shown

**Independent Test**: View the insights panel with insights displayed and verify a disclaimer line is always visible; review all template patterns to confirm no medical advice language

### Implementation for User Story 4

- [X] T020 [US4] Add a persistent non-medical disclaimer Text composable ("These are patterns in your data, not medical advice. Correlation does not imply causation.") in bodySmall from MaterialTheme.typography at the bottom of InsightsPanel's Success state; style as secondary color, non-dismissible, always visible when insights are shown per FR-007 in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightsPanel.kt

**Checkpoint**: Disclaimer text is always visible below insights; all insight text uses observational language only

---

## Phase 7: User Story 5 — Insights Update as New Data Is Tracked (Priority: P3)

**Goal**: Automatically refresh insights when users return to the Calendar screen after logging new daily entries

**Independent Test**: Log new daily entries via the DayPanel, navigate away from Calendar and back, verify the loading state briefly appears and insights update to reflect the new data

### Implementation for User Story 5

- [X] T021 [US5] Ensure InsightViewModel re-checks needsReanalysis() on each CalendarScreen appearance (not only ViewModel init) by triggering the analysis check from a LaunchedEffect in CalendarScreen keyed on the screen lifecycle or by exposing a refresh() function called from CalendarScreen's LaunchedEffect; when re-analysis is needed, emit Loading state, re-run analysis pipeline, then emit new results per FR-011 and SC-005 in BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt and BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/calendar/CalendarScreen.kt

**Checkpoint**: Logging new data and returning to Calendar triggers re-analysis with loading state; unchanged data shows cached insights instantly

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Error handling, edge cases, and final verification across all user stories

- [X] T022 Update InsightsPanel Error state to display a user-friendly message ("Couldn't generate insights right now. Try again later.") styled with M3 bodyMedium and secondary color per FR-014 in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightsPanel.kt
- [X] T023 Handle edge cases in CorrelationEngine and InsightViewModel: uniform data returns empty results with "No patterns found yet — try tracking varied data over time" message; division-by-zero protection in correlation formulas; gracefully catch analysis exceptions and emit Error state per spec edge cases in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/analysis/CorrelationEngine.kt and BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt
- [X] T024 Run quickstart.md verification scenarios: seed data verification, insights display, add-new-data refresh, cold restart cache, delete data type cleanup, fresh install empty state per specs/004-calendar-insights/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: N/A — no setup tasks needed
- **Foundational (Phase 2)**: No dependencies — can start immediately. T001–T006 are parallel; T007 depends on T001–T006
- **User Story 1 (Phase 3)**: Depends on Phase 2 completion — BLOCKS all other user stories
- **User Story 2 (Phase 4)**: Depends on Phase 3 (US1) — needs InsightsPanel to exist
- **User Story 3 (Phase 5)**: Depends on Phase 3 (US1) — needs InsightsPanel and InsightViewModel to exist
- **User Story 4 (Phase 6)**: Depends on Phase 3 (US1) — needs InsightsPanel to exist
- **User Story 5 (Phase 7)**: Depends on Phase 3 (US1) — needs InsightViewModel to exist
- **Polish (Phase 8)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2 — **MVP, all other stories depend on it**
- **User Story 2 (P1)**: Can start after US1 — independent of US3, US4, US5
- **User Story 3 (P2)**: Can start after US1 — independent of US2, US4, US5
- **User Story 4 (P2)**: Can start after US1 — independent of US2, US3, US5
- **User Story 5 (P3)**: Can start after US1 — independent of US2, US3, US4

### Within Each User Story

- Data layer before repository
- Repository before ViewModel
- ViewModel before UI components
- UI components before screen integration
- Composables with [P] can be built in parallel

### Parallel Opportunities

- Phase 2: T001–T006 are fully parallel (6 tasks, different files)
- Phase 3: T008 + T009 parallel (CorrelationEngine and InsightTextGenerator are independent); T012 parallel with T010/T011
- Phase 4–7: US2, US3, US4, US5 are independent of each other after US1 — can proceed in parallel if team capacity allows
- Phase 8: T022 and T023 are parallel (different files)

---

## Parallel Execution Example: Phase 2

```text
Timeline:
├── T001 InsightEntity.kt ─────────┐
├── T002 AnalysisMetadataEntity.kt ─┤
├── T003 InsightDao.kt ─────────────┤
├── T004 AnalysisMetadataDao.kt ────┤── All parallel
├── T005 DailyEntryDao.kt (modify) ─┤
├── T006 MultiChoiceSelectionDao.kt ┘
│
└── T007 AppDatabase.kt (modify) ← depends on T001–T006
```

## Parallel Execution Example: Phase 3 (User Story 1)

```text
Timeline:
├── T008 CorrelationEngine.kt ──────┐
├── T009 InsightTextGenerator.kt ───┤── Parallel
├── T012 InsightCard.kt ────────────┘
│
├── T010 InsightRepository.kt ← depends on Phase 2
├── T011 InsightViewModel.kt  ← depends on T008, T009, T010
├── T013 InsightsPanel.kt     ← depends on T011, T012
├── T014 BioGraphApplication.kt (modify) ← depends on T010
└── T015 CalendarScreen.kt (modify) ← depends on T013, T014
```

## Parallel Execution Example: Phases 4–7 (After US1)

```text
Timeline (all independent, can run in parallel):
├── US2: T016 → T017 (InsightLoader → InsightsPanel loading state)
├── US3: T018 + T019 parallel (InsightsPanel empty state + ViewModel threshold)
├── US4: T020 (InsightsPanel disclaimer)
└── US5: T021 (ViewModel re-check + CalendarScreen LaunchedEffect)
```

---

## Implementation Strategy

1. **MVP = Phase 2 + Phase 3 (User Story 1)**: Delivers the core value — insights visible on the Calendar screen. Ship and validate this before investing in polish stories.
2. **P1 complete = + Phase 4 (User Story 2)**: Adds the delightful loading experience. Together with US1, this completes the P1 priority scope.
3. **P2 complete = + Phase 5 + Phase 6 (US3 + US4)**: Handles edge cases for insufficient data and adds the non-medical disclaimer. These are independent and can be done in either order.
4. **Full feature = + Phase 7 + Phase 8 (US5 + Polish)**: Ensures insights stay fresh as data changes and covers all edge cases.
5. **Incremental delivery**: Each phase produces a working, testable increment. No phase leaves the app in a broken state.
