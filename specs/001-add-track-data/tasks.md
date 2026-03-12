# Tasks: Add and Track Data

**Input**: Design documents from `/specs/001-add-track-data/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, quickstart.md

**Tests**: Not mandated per constitution Principle IV (Simplicity & Pragmatism). No test tasks included.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Mobile (Android)**: All source files under `BioGraphApp/app/src/main/java/com/martynamaron/biograph/`
- **Gradle config**: `BioGraphApp/gradle/libs.versions.toml`, `BioGraphApp/app/build.gradle.kts`, `BioGraphApp/build.gradle.kts`
- **Manifest**: `BioGraphApp/app/src/main/AndroidManifest.xml`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add all new dependencies and configure the build system for Room, Navigation Compose, Lifecycle, and java.time desugaring.

- [ ] T001 Add Room, KSP, Navigation Compose, Lifecycle ViewModel Compose, Lifecycle Runtime Compose, and Desugaring version entries and library aliases to BioGraphApp/gradle/libs.versions.toml
- [ ] T002 Apply KSP plugin in BioGraphApp/build.gradle.kts (project-level) and configure Room schema export, core library desugaring, and all new dependency declarations in BioGraphApp/app/build.gradle.kts

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Room database layer, repository layer, Application class, navigation skeleton, and MainActivity wiring. MUST be complete before ANY user story can be implemented.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T003 [P] Create DataTypeEntity Room entity with id, emoji, description, createdAt fields and composite unique index on (emoji, description) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/DataTypeEntity.kt
- [ ] T004 [P] Create DailyEntryEntity Room entity with id, date, dataTypeId fields, foreign key to DataTypeEntity with CASCADE delete, and composite unique index on (date, dataTypeId) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/DailyEntryEntity.kt
- [ ] T005 [P] Create DataTypeDao interface with getAllFlow(), getCount(), insert(), update(), delete(), and findDuplicate() methods in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/DataTypeDao.kt
- [ ] T006 [P] Create DailyEntryDao interface with getEntriesForDateFlow(), getDatesWithEntriesFlow(), insertAll(), deleteAllForDate(), replaceEntriesForDate() transaction, and deleteOrphanedEntries() in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/DailyEntryDao.kt
- [ ] T007 Create AppDatabase Room database class (version 1) referencing DataTypeEntity and DailyEntryEntity with schema export enabled and destructive migration fallback in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/AppDatabase.kt
- [ ] T008 [P] Create DataTypeRepository wrapping DataTypeDao with Flow-based getAllFlow(), suspend getCount(), insert with duplicate check, update, and delete in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/repository/DataTypeRepository.kt
- [ ] T009 [P] Create DailyEntryRepository wrapping DailyEntryDao with getEntriesForDate(), getDatesWithEntries(), and replaceEntriesForDate() in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/repository/DailyEntryRepository.kt
- [ ] T010 Create BioGraphApplication class extending Application with lazy Room database singleton and repository accessors in BioGraphApp/app/src/main/java/com/martynamaron/biograph/BioGraphApplication.kt
- [ ] T011 Register BioGraphApplication as android:name on the application tag in BioGraphApp/app/src/main/AndroidManifest.xml
- [ ] T012 Create NavGraph composable with route definitions (Onboarding, Calendar, DataTypeList, Settings) using serializable route objects and AnimatedNavHost with slide+fade transitions in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/navigation/NavGraph.kt
- [ ] T013 Update MainActivity to set Compose content hosting the NavGraph with MyApplicationTheme in BioGraphApp/app/src/main/java/com/martynamaron/biograph/MainActivity.kt

**Checkpoint**: Foundation ready — database, repositories, navigation skeleton, and Application class all in place. User story implementation can now begin.

---

## Phase 3: User Story 1 — Define a Custom Data Type (Priority: P1) 🎯 MVP

**Goal**: Users can create a new data type by selecting an emoji and entering a description, with validation and duplicate prevention.

**Independent Test**: Open app → navigate to data type list → tap Add → select emoji → enter description → save → confirm new type appears in list. Verify validation blocks save without emoji or description. Verify duplicate is rejected.

### Implementation for User Story 1

- [ ] T014 [P] [US1] Create EmojiPicker composable with a single-line TextField configured for emoji keyboard input and single-emoji validation in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/EmojiPicker.kt
- [ ] T015 [US1] Create DataTypeViewModel with StateFlow UI state, add data type action with emoji+description validation (non-empty, max 60 chars), duplicate check via repository, and error state handling in BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/DataTypeViewModel.kt
- [ ] T016 [US1] Create DataTypeEditDialog composable with EmojiPicker, description TextField with 60-char counter, inline validation messages, and Save/Cancel buttons in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/datatype/DataTypeEditDialog.kt
- [ ] T017 [US1] Create DataTypeListScreen composable with scrollable list of data types (emoji + description, newest first), FAB to open DataTypeEditDialog, and empty-state prompt in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/datatype/DataTypeListScreen.kt
- [ ] T018 [US1] Wire DataTypeListScreen route into NavGraph and add navigation action from CalendarScreen to DataTypeListScreen in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/navigation/NavGraph.kt

**Checkpoint**: User Story 1 complete — users can create and view custom data types. This is the MVP.

---

## Phase 4: User Story 2 — Log Daily Data via Calendar (Priority: P1)

**Goal**: Users see a monthly calendar, tap a day to open a bottom sheet with data type toggles, save selections, and see visual indicators on logged days.

**Independent Test**: Open app → calendar shows current month with today highlighted → tap a day → bottom sheet opens with all data types as toggles → toggle some on → tap Save → day shows dot indicator → tap same day again → toggles reflect saved state → swipe to navigate months with animated transition.

### Implementation for User Story 2

- [ ] T019 [P] [US2] Create CalendarDay composable rendering day number, today highlight, and activity indicator dot with ripple touch feedback in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/CalendarDay.kt
- [ ] T020 [P] [US2] Create DataTypeToggleItem composable with emoji, description text, and Material 3 Switch toggle in a 48dp-tall row in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/components/DataTypeToggleItem.kt
- [ ] T021 [US2] Create CalendarViewModel with current YearMonth state, month navigation (prev/next), selected day state, entries-for-day Flow, dates-with-entries Flow for indicator dots, and save-day-entries action in BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/CalendarViewModel.kt
- [ ] T022 [US2] Create DayPanel ModalBottomSheet composable displaying DataTypeToggleItem list for selected day with local toggle state, Save/Done button committing via CalendarViewModel, and dismiss-without-save behavior in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/calendar/DayPanel.kt
- [ ] T023 [US2] Create CalendarScreen composable with 7-column LazyVerticalGrid of CalendarDay cells, month/year header with prev/next navigation arrows, AnimatedContent for month slide transitions, and day-tap triggering DayPanel in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/calendar/CalendarScreen.kt
- [ ] T024 [US2] Set CalendarScreen as the default start destination in NavGraph and ensure navigation to DataTypeList is accessible from CalendarScreen in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/navigation/NavGraph.kt

**Checkpoint**: User Stories 1 AND 2 complete — users can define data types and log daily data via the calendar. Core app loop is functional.

---

## Phase 5: User Story 3 — Edit or Delete a Data Type (Priority: P2)

**Goal**: Users can edit the emoji or description of an existing data type, or delete it (with confirmation) which cascades to remove all associated daily entries.

**Independent Test**: Open data type list → tap edit on a type → change emoji or description → save → confirm update reflected in list and calendar day panels. Tap delete → confirmation dialog appears → confirm → type and all its historical entries removed. Cancel delete → type remains.

### Implementation for User Story 3

- [ ] T025 [US3] Add update and delete actions to DataTypeViewModel with delete-confirmation state (pending delete target), confirm-delete action triggering repository delete, and edit validation (same rules as add, excluding self from duplicate check) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/DataTypeViewModel.kt
- [ ] T026 [US3] Update DataTypeEditDialog to accept an optional existing DataType for edit mode, pre-filling emoji and description fields, and changing dialog title to "Edit Data Type" in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/datatype/DataTypeEditDialog.kt
- [ ] T027 [US3] Add edit button and delete button with AlertDialog confirmation (stating historical data will be lost) to each item in DataTypeListScreen in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/datatype/DataTypeListScreen.kt

**Checkpoint**: User Stories 1, 2, AND 3 complete — full CRUD for data types with calendar integration.

---

## Phase 6: User Story 4 — New-User Onboarding (Priority: P2)

**Goal**: First-time users see an onboarding flow with pre-defined data type suggestions they can select, then proceed to the calendar. Skippable.

**Independent Test**: Clear app data → launch app → onboarding screen appears with 9 suggestions → select some → tap "Get Started" → selected types created → navigated to calendar. Re-launch → onboarding does NOT appear. Test skip: clear data → launch → tap Skip → calendar shown with no data types.

### Implementation for User Story 4

- [ ] T028 [P] [US4] Create OnboardingSuggestion data class and DEFAULT_SUGGESTIONS list (9 items: period/bleeding, mood, medication, sleep quality, exercise, left the house, saw friends, headache, energy level) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/OnboardingSuggestion.kt
- [ ] T029 [US4] Create OnboardingViewModel with selected-suggestions state, toggle-selection action, and create-selected-types action that batch-inserts via DataTypeRepository then signals navigation to calendar in BioGraphApp/app/src/main/java/com/martynamaron/biograph/viewmodel/OnboardingViewModel.kt
- [ ] T030 [US4] Create OnboardingScreen composable with welcome message, selectable suggestion chips (emoji + description), "Get Started" button, "Skip" option, and "you can add more later" text in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/onboarding/OnboardingScreen.kt
- [ ] T031 [US4] Update NavGraph to conditionally route to OnboardingScreen on app start when DataTypeRepository.getCount() returns 0, otherwise route to CalendarScreen in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/navigation/NavGraph.kt

**Checkpoint**: User Stories 1–4 complete — new users get onboarding, existing users go straight to calendar.

---

## Phase 7: User Story 5 — Generate Mock Data (Priority: P3)

**Goal**: Users can generate 2 months of randomised daily entries from a settings screen for demo/evaluation purposes.

**Independent Test**: Create at least one data type → open Settings → tap "Generate Mock Data" → confirmation shown → confirm → navigate calendar to previous 2 months → verify entries exist with varied distribution (not all-on or all-off). Tap generate again → overwrite warning shown.

### Implementation for User Story 5

- [ ] T032 [P] [US5] Create MockDataGenerator utility class that accepts DataTypeRepository and DailyEntryRepository, generates entries for previous 2 calendar months with ~40-60% random toggle probability per data type per day in BioGraphApp/app/src/main/java/com/martynamaron/biograph/util/MockDataGenerator.kt
- [ ] T033 [US5] Add RoomDatabase.Callback.onCreate to AppDatabase that pre-seeds the database with default data type suggestions and 2 months of mock daily entries via MockDataGenerator so the demo works immediately after install (Constitution Principle V) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/data/local/AppDatabase.kt
- [ ] T034 [US5] Create SettingsScreen composable with "Generate Mock Data" button, overwrite-confirmation AlertDialog, no-data-types guard prompting user to create types first, and success Snackbar in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/settings/SettingsScreen.kt
- [ ] T035 [US5] Wire SettingsScreen route into NavGraph and add settings navigation action (e.g., gear icon) from CalendarScreen top bar in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/navigation/NavGraph.kt

**Checkpoint**: All 5 user stories complete — full feature set implemented. Database is auto-seeded on first install per Constitution Principle V.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Animation review, edge-case hardening, and build validation across all user stories.

- [ ] T036 [P] Review and verify all screen transitions in NavGraph use slide+fade animated enter/exit (300ms EaseInOut) and calendar month navigation uses AnimatedContent slide transition per research.md
- [ ] T037 [P] Verify edge cases: calendar handles 12+ months backward navigation, day panel scrolls with 30 data types without clipping, long descriptions truncate gracefully, empty-day-panel shows create-type prompt
- [ ] T038 Run quickstart.md build validation: execute ./gradlew assembleDebug from BioGraphApp/ and confirm clean build with no errors

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — **BLOCKS all user stories**
- **User Story 1 (Phase 3)**: Depends on Phase 2 completion
- **User Story 2 (Phase 4)**: Depends on Phase 2 completion; benefits from US1 being done (to have data types to toggle) but structurally independent
- **User Story 3 (Phase 5)**: Depends on Phase 3 (US1) — extends DataTypeViewModel/screens
- **User Story 4 (Phase 6)**: Depends on Phase 2 completion; uses DataTypeRepository
- **User Story 5 (Phase 7)**: Depends on Phase 2 completion; uses both repositories
- **Polish (Phase 8)**: Depends on all desired user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational — no dependency on other stories
- **US2 (P1)**: Can start after Foundational — no dependency on other stories (but best after US1 for end-to-end testing)
- **US3 (P2)**: Depends on US1 (extends its ViewModel and screens)
- **US4 (P2)**: Can start after Foundational — independent of US1/US2/US3
- **US5 (P3)**: Can start after Foundational — independent (needs data types to exist at runtime but not at build time)

### Within Each User Story

- Components ([P] marked) before ViewModels that use them
- ViewModels before Screens that consume their state
- Screens before NavGraph wiring
- Core implementation before integration

### Parallel Opportunities

**Phase 2 parallel batch**: T003 + T004 + T005 + T006 (all entity/DAO files, no cross-dependencies)
**Phase 2 parallel batch**: T008 + T009 (repositories, after DAOs exist)
**Phase 3 parallel**: T014 (EmojiPicker) can be built alongside Phase 2 tasks
**Phase 4 parallel batch**: T019 + T020 (CalendarDay + DataTypeToggleItem, independent components)
**Cross-story parallel**: US1 (Phase 3) and US4 (Phase 6) can proceed in parallel after Foundational
**Cross-story parallel**: US2 (Phase 4) and US5 (Phase 7) can proceed after their dependencies

---

## Parallel Example: Phase 2 Foundation

```
# Parallel batch 1 — all entities and DAOs (4 files, no cross-deps):
Task T003: "Create DataTypeEntity in data/local/DataTypeEntity.kt"
Task T004: "Create DailyEntryEntity in data/local/DailyEntryEntity.kt"
Task T005: "Create DataTypeDao in data/local/DataTypeDao.kt"
Task T006: "Create DailyEntryDao in data/local/DailyEntryDao.kt"

# Sequential — AppDatabase depends on entities + DAOs:
Task T007: "Create AppDatabase in data/local/AppDatabase.kt"

# Parallel batch 2 — repositories (2 files, depend on DAOs):
Task T008: "Create DataTypeRepository in data/repository/DataTypeRepository.kt"
Task T009: "Create DailyEntryRepository in data/repository/DailyEntryRepository.kt"

# Sequential — Application depends on AppDatabase:
Task T010: "Create BioGraphApplication in BioGraphApplication.kt"
Task T011: "Register BioGraphApplication in AndroidManifest.xml"

# Parallel — NavGraph and MainActivity (independent of DB layer):
Task T012: "Create NavGraph in ui/navigation/NavGraph.kt"
Task T013: "Update MainActivity.kt"
```

## Parallel Example: User Story 2

```
# Parallel — independent UI components:
Task T019: "Create CalendarDay component"
Task T020: "Create DataTypeToggleItem component"

# Sequential — ViewModel needs repositories:
Task T021: "Create CalendarViewModel"

# Sequential — DayPanel uses ToggleItem + ViewModel:
Task T022: "Create DayPanel bottom sheet"

# Sequential — CalendarScreen uses CalendarDay + ViewModel + DayPanel:
Task T023: "Create CalendarScreen"

# Sequential — NavGraph wiring:
Task T024: "Set CalendarScreen as default in NavGraph"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (2 tasks)
2. Complete Phase 2: Foundational (11 tasks) — **CRITICAL, blocks all stories**
3. Complete Phase 3: User Story 1 (5 tasks)
4. **STOP and VALIDATE**: User can create and view data types
5. Demo/deploy if ready

### Incremental Delivery

1. Setup + Foundational → Foundation ready (13 tasks)
2. Add User Story 1 → Data type CRUD works (5 tasks) → **MVP!**
3. Add User Story 2 → Calendar + daily logging works (6 tasks) → **Core app loop!**
4. Add User Story 3 → Edit/delete data types (3 tasks)
5. Add User Story 4 → Onboarding flow (4 tasks)
6. Add User Story 5 → Mock data generation + auto pre-seeding (4 tasks)
7. Polish → Animation review + edge cases + build validation (3 tasks)
8. Each story adds value without breaking previous stories

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- No test tasks included (not mandated by constitution or spec)
- Total: 38 tasks across 8 phases
