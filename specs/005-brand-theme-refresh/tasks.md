# Tasks: Brand Theme Refresh

**Input**: Design documents from `/specs/005-brand-theme-refresh/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, quickstart.md

**Tests**: Not requested in the feature specification. No test tasks included.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: Define the brand colour palette in the Compose theme system

- [X] T001 Replace purple/pink colour definitions with brand green palette (GreenDarkest #15603E, GreenMid #1E8D5A, GreenLightest #6CC19F, GrayBackground #D4D2D2) and derived tones (SecondaryContainer #B8E6D0, TertiaryContainer #E0F5EC) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/theme/Color.kt
- [X] T002 Remove unused purple and teal colour definitions (purple_200, purple_500, purple_700, teal_200, teal_700) from BioGraphApp/app/src/main/res/values/colors.xml, keeping green_darkest, green_mid, green_lightest, gray_background, black, and white

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Wire the brand colours into the Material 3 theme so ALL screens inherit them automatically

**⚠️ CRITICAL**: No user story work can begin until this phase is complete — the theme must be in place before modifying individual screens

- [X] T003 Define complete LightColorScheme in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/theme/Theme.kt using the Material 3 role mapping from research.md (primary=GreenMid, secondary=GreenDarkest, tertiary=GreenLightest, surfaceVariant=GrayBackground, and all on* counterparts per the contrast-verified mapping)
- [X] T004 Remove DarkColorScheme, dynamic colour logic (dynamicColor parameter, Build.VERSION check, dynamicLightColorScheme/dynamicDarkColorScheme), and isSystemInDarkTheme import from BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/theme/Theme.kt — simplify MyApplicationTheme to always apply LightColorScheme

**Checkpoint**: Build and run the app — all 4 screens (Onboarding, Calendar, Data Type List, Settings) should now display green brand colours on all interactive elements, top bars, and surfaces. No purple/pink should be visible anywhere.

---

## Phase 3: User Story 1 — Cohesive Green Brand Theme (Priority: P1) 🎯 MVP

**Goal**: Every screen uses the green brand palette consistently — buttons, top bars, toggles, chips, selected states, and surfaces all reflect the brand identity.

**Independent Test**: Launch the app, navigate all 4 screens, and verify all interactive elements and surfaces use green tones. No purple/pink defaults visible.

### Implementation for User Story 1

- [X] T005 [US1] Verify all interactive elements (buttons, FABs, toggles, chips, selected states) across all 4 screens display brand green colours by running the app and performing a visual walkthrough — document any elements that do not inherit the theme correctly
- [X] T006 [US1] Fix any composables that use hardcoded colours or do not properly inherit from MaterialTheme.colorScheme — ensure all colour references use theme roles (primary, secondary, primaryContainer, etc.) rather than literal Color(...) values

**Checkpoint**: User Story 1 is complete — the entire app uses the green brand palette. This is the MVP.

---

## Phase 4: User Story 2 — Logo on the Onboarding Screen (Priority: P2)

**Goal**: New users see the BioGraph leaf logo prominently at the top-center of the Onboarding screen, above the welcome headline.

**Independent Test**: Clear app data, launch the app, and verify the logo appears at the top-center of the Onboarding screen with a fade-in animation.

### Implementation for User Story 2

- [X] T007 [US2] Add brand logo Image composable (painterResource(R.drawable.black_logo), ~120dp × 150dp, ContentScale.Fit) at the top-center of the OnboardingScreen column, replacing the existing Spacer(48.dp) — wrap in AnimatedVisibility with fadeIn for a polished entrance in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/onboarding/OnboardingScreen.kt
- [X] T008 [US2] Add @Preview annotation for OnboardingScreen composable showing the logo placement in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/onboarding/OnboardingScreen.kt

**Checkpoint**: User Story 2 is complete — the logo is visible on the Onboarding screen with a fade-in animation.

---

## Phase 5: User Story 3 — Logo in the Top App Bar (Priority: P3)

**Goal**: Returning users see a small leaf logo icon + "BioGraph" text (icon-wordmark combo) in the Calendar screen's top app bar.

**Independent Test**: Navigate to the Calendar screen and verify the leaf icon + "BioGraph" text are visible in the top app bar alongside the existing Settings and Data Types action icons.

### Implementation for User Story 3

- [X] T009 [US3] Replace the TopAppBar title from Text("BioGraph") to a Row containing Image(painterResource(R.drawable.black_logo), Modifier.size(24.dp, 30.dp)), Spacer(8.dp), and Text("BioGraph", style = MaterialTheme.typography.titleLarge) in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/calendar/CalendarScreen.kt
- [X] T010 [US3] Add @Preview annotation for the CalendarScreen TopAppBar showing the icon-wordmark layout in BioGraphApp/app/src/main/java/com/martynamaron/biograph/ui/screens/calendar/CalendarScreen.kt

**Checkpoint**: User Story 3 is complete — the icon-wordmark combo is visible in the Calendar screen's top app bar.

---

## Phase 6: User Story 4 — Updated App Launcher Icon (Priority: P4)

**Goal**: The home screen launcher icon uses the leaf logo on a brand grey background, matching the refreshed in-app branding.

**Independent Test**: Install the app and check the launcher icon on the home screen and app drawer — it should show the leaf logo on a grey background in all adaptive icon shapes.

### Implementation for User Story 4

- [X] T011 [P] [US4] Replace the Android robot vector in BioGraphApp/app/src/main/res/drawable/ic_launcher_foreground.xml with the leaf logo path data from black_logo.xml, scaled and centered for the 108dp adaptive icon viewport (66dp safe zone)
- [X] T012 [P] [US4] Replace the #3DDC84 green grid pattern in BioGraphApp/app/src/main/res/drawable/ic_launcher_background.xml with a solid #D4D2D2 grey fill

**Checkpoint**: User Story 4 is complete — the launcher icon shows the leaf logo on grey across round, squircle, and square shapes.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and cleanup across all user stories

- [X] T013 Run full visual walkthrough of all 4 screens in both phone (360dp) and large phone (430dp) widths to verify consistent branding, no purple/pink remnants, logo rendering, and no layout overflow
- [X] T014 Run quickstart.md verification checklist to confirm all 7 implementation steps are complete

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 (Color.kt must be defined before Theme.kt references it) — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 — visual verification and fixups
- **US2 (Phase 4)**: Depends on Phase 2 — can run in parallel with US1, US3, US4
- **US3 (Phase 5)**: Depends on Phase 2 — can run in parallel with US1, US2, US4
- **US4 (Phase 6)**: No dependency on Phase 2 (XML resources only) — can run in parallel with any phase after Phase 1
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Depends on Foundational (Phase 2) — no dependencies on other stories
- **User Story 2 (P2)**: Depends on Foundational (Phase 2) — no dependencies on other stories
- **User Story 3 (P3)**: Depends on Foundational (Phase 2) — no dependencies on other stories
- **User Story 4 (P4)**: Depends on Setup (Phase 1) only — XML-only changes, independent of Compose theme

### Parallel Opportunities

- T001 and T002 (Setup) touch different files and can run in parallel
- T011 and T012 (US4) touch different files and can run in parallel
- US2 (Phase 4), US3 (Phase 5), and US4 (Phase 6) can all proceed in parallel after their dependencies are met

---

## Parallel Example: After Foundational Phase

```
# These can all start simultaneously after Phase 2:
Task T005: [US1] Visual walkthrough verification
Task T007: [US2] Add logo to OnboardingScreen
Task T009: [US3] Add icon-wordmark to CalendarScreen TopAppBar

# US4 can start even earlier (after Phase 1):
Task T011: [US4] Replace launcher foreground
Task T012: [US4] Replace launcher background
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001, T002)
2. Complete Phase 2: Foundational (T003, T004)
3. Complete Phase 3: User Story 1 (T005, T006)
4. **STOP and VALIDATE**: Build, run, navigate all screens — green palette everywhere, no purple
5. This is the MVP — the app looks branded

### Incremental Delivery

1. Setup + Foundational → Brand palette wired into theme
2. Add US1 → Visual verification → **MVP ready**
3. Add US2 → Logo on Onboarding → Brand first impression
4. Add US3 → Icon-wordmark in Calendar → Daily brand presence
5. Add US4 → Launcher icon → External branding complete
6. Polish → Final walkthrough → Feature complete
