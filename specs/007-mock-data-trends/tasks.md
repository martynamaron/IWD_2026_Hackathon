# Tasks: Mock Data Generation & Insight Trends

**Input**: Design documents from `/specs/007-mock-data-trends/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, quickstart.md

**Tests**: Not requested — testing is not mandated per constitution Principle IV: Simplicity & Pragmatism.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

- **Base path**: `BioGraphApp/app/src/main/java/com/martynamaron/biograph/`
- **Mock generator**: `util/MockDataGenerator.kt`
- **ViewModel**: `viewmodel/InsightViewModel.kt`
- **UI components**: `ui/components/`
- **Data/analysis**: `data/analysis/`

---

## Phase 1: Setup

**Purpose**: No new project setup needed — the project already exists with all required dependencies (Room, Jetpack Compose, Navigation, Material 3). No new libraries are introduced by this feature.

*No tasks in this phase.*

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create new data structures that the trend indicator UI (US3) depends on. These are UI-only types not persisted in Room.

**⚠️ CRITICAL**: Phase 6 (US3) cannot begin until these are in place.

- [X] T001 [P] Create TrendDirection enum (STRENGTHENING, WEAKENING, STABLE) with display label and arrow icon mappings in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/analysis/TrendDirection.kt
- [X] T002 [P] Create InsightWithTrend data class wrapping InsightEntity with optional TrendDirection in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/analysis/InsightWithTrend.kt

**Checkpoint**: Foundation ready — TrendDirection and InsightWithTrend available for use in ViewModel and UI layers.

---

## Phase 3: User Story 4 — Updated Data Types in Mock Data (Priority: P1)

**Goal**: Define the 6 new health-focused data types (Health, Medication, Mood, Energy Levels, Exercise, Period Bleeding) with correct input types, emojis, and multiple-choice options, replacing the previous demo data types.

**Independent Test**: Generate mock data and verify exactly 6 data types exist with correct names, input types (MULTIPLE_CHOICE, TOGGLE, SCALE), and options per FR-003.

### Implementation for User Story 4

- [X] T003 [US4] Define the 6 data type configurations in MockDataGenerator — Health (MC: headache, stomachache, feeling dizzy, vomiting, muscle aches, tiredness), Medication (TOGGLE), Mood (SCALE), Energy Levels (SCALE), Exercise (MC: long walk, swimming, dancing, tennis), Period Bleeding (TOGGLE) — with emojis per data-model.md in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt

**Checkpoint**: MockDataGenerator knows the 6 new data types. Running generation creates the correct DataTypeEntity and MultipleChoiceOptionEntity rows.

---

## Phase 4: User Story 1 — Generating Realistic Mock Data (Priority: P1) 🎯 MVP

**Goal**: Users can choose to generate 3 or 6 months of historical daily tracking data across all 6 data types with natural variation — the core data layer everything else depends on.

**Independent Test**: Select each time range option and verify entries exist for every day in the chosen range across all 6 data types, with values that vary naturally day-to-day.

### Implementation for User Story 1

- [X] T004 [US1] Rewrite MockDataGenerator.generate() signature to accept a months parameter (3 or 6) instead of a data type list, and internally create all 6 data types via repositories in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt
- [X] T005 [US1] Implement clear-and-regenerate logic: delete all existing data types, daily entries, multi-choice selections, and insights before generating new data (silent replace per FR-004a) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt
- [X] T006 [US1] Implement daily entry generation loop from today back N months for all 6 data types with natural variation — not every type needs an entry every day, scale values vary, MC selections vary (FR-004) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt
- [X] T007 [US1] Update the mock data generation call site to present "3 months" and "6 months" options and pass the selected month count to MockDataGenerator.generate() — find and update the caller in the onboarding or settings flow

**Checkpoint**: Users can select 3 or 6 months, data populates the calendar with varied daily entries across all 6 data types.

---

## Phase 5: User Story 2 — Exploring Health Correlations in Mock Data (Priority: P1)

**Goal**: The generated mock data contains 5 embedded correlation patterns (medication–dizziness, medication–mood, menstrual cycle–mood/energy, exercise–tiredness, exercise–tiredness weakening) that the existing CorrelationEngine will detect and surface as insights.

**Independent Test**: Generate 6 months of mock data, open the insights panel for different time periods, and verify that at least 4 of the 5 designed correlations appear with appropriate strength.

**Depends on**: US4 (data types defined), US1 (generation loop working)

### Implementation for User Story 2

- [X] T008 [US2] Implement menstrual cycle pre-computation: anchor cycle to random start day in first 7 days, repeat with cycle length sampled from N(28, 1.5) clamped to 26–30, bleeding duration 4–6 days per cycle, and compute pre-menstrual window (days −3 to −1 before each bleed start) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt
- [X] T009 [US2] Implement medication schedule generation: ~75% daily compliance overall with 2–3 intentional missed streaks of 3–5 consecutive days per 3-month period, transitioning to ~95% compliance after the first ~2 months in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt
- [X] T010 [US2] Implement medication–dizziness correlation: when medication is missed for 3+ consecutive days, significantly increase P(feeling dizzy) in Health MC selections for the streak duration and 1–2 days after (FR-005) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt
- [X] T011 [US2] Implement medication–mood improvement: track running medication consistency score (55+ of last 60 days = consistent), elevate mood base by +1.5 to +2.0 scale points when consistent, creating a transition around the 2-month mark (FR-006) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt
- [X] T012 [US2] Implement PMS mood/energy dip: apply −1.5 to −2.0 penalty to Mood and Energy Levels scale values during pre-menstrual window (days −3 to −1) and first 1–2 days of bleeding, clamp to 0–5 range (FR-008) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt
- [X] T013 [US2] Implement exercise–tiredness correlation with temporal weakening: P(tiredness | exercise) decays linearly from ~0.80 at range start to ~0.25 at today, baseline P(tiredness) stays ~0.15 (FR-009, FR-010) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt

**Checkpoint**: After generating 6 months of data, the insights panel shows correlations for medication–dizziness, medication–mood, period–mood/energy, and exercise–tiredness. The exercise–tiredness pattern is noticeably stronger in older data.

---

## Phase 6: User Story 3 — Viewing Trend Indicators on Insights (Priority: P1)

**Goal**: Insight cards in the "Last 3 Months" and "Last Year" views display trend indicators (↑ Strengthening, ↓ Weakening, → Stable) showing how correlation strength has changed over the viewed period. No trend indicators appear in the "Last Month" view.

**Independent Test**: Generate 6 months of mock data, switch to "Last 3 Months" or "Last Year" view, and verify at least one insight shows a trend indicator (e.g., exercise–tiredness shows ↓ Weakening).

**Depends on**: Foundational (T001–T002), US1 (data exists), US2 (correlation patterns embedded)

### Implementation for User Story 3

- [X] T014 [US3] Add trend computation to InsightViewModel: after computing full-range insights, split the viewed date range at the midpoint, run CorrelationEngine.analyseAll() on each half, match result pairs by data type IDs, compute Δ = |coeff_second| − |coeff_first|, assign TrendDirection (Δ ≥ 0.15 → STRENGTHENING, Δ ≤ −0.15 → WEAKENING, else STABLE), skip entirely for LAST_MONTH (FR-011, FR-014, FR-015) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt
- [X] T015 [US3] Update InsightSortState sealed class: change ByStrength to hold List<InsightWithTrend> instead of List<InsightEntity>, and update ByDataType to use GroupedInsightWithTrend (adding trend field to GroupedInsight or replacing it), then update buildGroupedState() accordingly in BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/InsightViewModel.kt
- [X] T016 [US3] Add optional TrendDirection parameter to InsightCard composable and render trend indicator as a Row with small directional arrow icon (Icons.Default: KeyboardArrowUp / KeyboardArrowDown / ArrowForward) and text label ("Strengthening" / "Weakening" / "Stable") using MaterialTheme.typography.labelSmall and semantic colors (FR-013) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightCard.kt
- [X] T017 [P] [US3] Update InsightsByStrength to accept List<InsightWithTrend>, extract trend for each item, and pass TrendDirection to InsightCard in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightsByStrength.kt
- [X] T018 [P] [US3] Update InsightsByDataType to accept groups containing trend data (GroupedInsightWithTrend or updated GroupedInsight) and pass TrendDirection to InsightCard in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightsByDataType.kt
- [X] T019 [US3] Update InsightsPanel to thread the updated trend-aware InsightSortState through to InsightsByStrength and InsightsByDataType composables in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/InsightsPanel.kt

**Checkpoint**: In "Last 3 Months" or "Last Year" views, insight cards display trend arrows. Exercise–tiredness shows ↓ Weakening. No trends appear in "Last Month" view.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: End-to-end validation and cleanup

- [X] T020 Verify all 5 correlation patterns produce coefficients above the 0.35 threshold by generating 6 months of data and inspecting insight panel output
- [X] T021 Run quickstart.md validation steps: build, install, generate both time ranges, verify calendar entries, correlations in insights, and trend indicators

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: N/A — existing project, no setup needed
- **Foundational (Phase 2)**: No dependencies — can start immediately. BLOCKS Phase 6 (US3).
- **US4 (Phase 3)**: No dependencies — can start immediately (or in parallel with Phase 2). BLOCKS US1.
- **US1 (Phase 4)**: Depends on US4 (data types must be defined). BLOCKS US2.
- **US2 (Phase 5)**: Depends on US1 (generation loop must exist to add correlation patterns).
- **US3 (Phase 6)**: Depends on Phase 2 (TrendDirection types) and ideally US2 (to verify trends with real correlation data). Code changes are independent of US2.
- **Polish (Phase 7)**: Depends on all phases complete.

### User Story Dependencies

```
Phase 2 (Foundational) ──────────────────────────────┐
                                                      ▼
Phase 3 (US4) → Phase 4 (US1) → Phase 5 (US2) → Phase 6 (US3) → Phase 7 (Polish)
```

- **US4** → **US1**: Data type definitions required before generation loop
- **US1** → **US2**: Generation loop required before embedding correlation patterns
- **US2** ∥ **US3**: Trend indicator code (US3) can be developed in parallel with correlation patterns (US2) since the trend computation uses CorrelationEngine independently. However, end-to-end testing of trend indicators requires US2 data.
- **Phase 2** → **US3**: TrendDirection and InsightWithTrend types required for ViewModel/UI work

### Within Each User Story

- Models/data structures before services/logic
- ViewModel changes before UI changes
- Core implementation before integration plumbing

### Parallel Opportunities

**Phase 2** (Foundational):
```
T001 (TrendDirection enum)  ──┐
                              ├── both in parallel (different files)
T002 (InsightWithTrend)     ──┘
```

**Phase 6** (US3 — after T014, T015, T016 complete):
```
T017 (InsightsByStrength)   ──┐
                              ├── both in parallel (different files)
T018 (InsightsByDataType)   ──┘
```

**Cross-story parallelism** (if team capacity allows):
```
Phase 2 + Phase 3 (US4) can start simultaneously
Phase 5 (US2) + Phase 6 (US3) code changes can proceed in parallel
```

---

## Implementation Strategy

### MVP First (User Story 4 + User Story 1)

1. Complete Phase 2: Foundational (T001–T002)
2. Complete Phase 3: US4 — data type definitions (T003)
3. Complete Phase 4: US1 — mock data generation with time ranges (T004–T007)
4. **STOP and VALIDATE**: Verify 6 data types created, daily entries populate calendar for both 3-month and 6-month ranges
5. App is usable with varied data — insights will show but without designed correlations

### Incremental Delivery

1. US4 + US1 → Mock data works → Calendar populated (MVP)
2. Add US2 → Meaningful correlations in insights panel → Demo-quality experience
3. Add US3 → Trend indicators on insight cards → Full feature complete
4. Polish → End-to-end validation → Ship-ready

### Single Developer Flow

1. T001, T002 in parallel → Foundation types
2. T003 → Data type definitions
3. T004 → T005 → T006 → T007 → Generation loop + call site
4. T008 → T009 → T010 → T011 → T012 → T013 → All correlation patterns
5. T014 → T015 → T016 → Trend computation + card UI
6. T017, T018 in parallel → List composable updates
7. T019 → InsightsPanel threading
8. T020 → T021 → Validation
