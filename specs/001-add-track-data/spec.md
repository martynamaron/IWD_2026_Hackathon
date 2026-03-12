# Feature Specification: Add and Track Data

**Feature Branch**: `001-add-track-data`
**Created**: 2026-03-12
**Status**: Draft
**Input**: User description: "Build an app called BioGraph which lets users input daily data about themselves. Users define custom data types represented by emoji + description (e.g., period bleeding, body aches, mood, medication, sleep quality, leaving the house, seeing friends). Calendar interface to log data against days. Editable/deletable data types. Onboarding for new users with pre-defined suggestions. Mock data generation for 2 months."

## Clarifications

### Session 2026-03-12

- Q: What level of data protection should the app provide for on-device storage? → A: No special protection — rely on Android OS-level encryption only.
- Q: Which local storage technology should the app use for persisting data types and daily entries? → A: Room (SQLite-based, Jetpack standard).
- Q: Which UI framework should the app use? → A: Jetpack Compose (modern declarative UI).
- Q: How should data types be toggled on the day panel — auto-save or explicit save? → A: Explicit "Save" / "Done" button to commit all changes at once.
- Q: How should data types be ordered in the list and on the day panel? → A: Fixed order by creation date, newest first.

## Assumptions

- This is a single-user, on-device app — no accounts, authentication, or multi-device sync.
- Data protection relies solely on Android OS-level device encryption; no additional app-level encryption or lock screen is implemented.
- Local persistence uses Room (SQLite-based, Jetpack standard) for all structured data (data types and daily entries).
- The UI is built entirely with Jetpack Compose (no XML Views/Fragments).
- Data types are displayed in reverse chronological order (newest first) in both the data-type list and the day panel.
- Data types are binary or simple toggles per day (e.g., "did I take my meds" = yes/no). More granular scales (1–5 ratings, numeric values) are out of scope for this phase.
- Emoji selection uses the device's native emoji keyboard or a curated picker within the app.
- The calendar view defaults to the current month with the ability to navigate to previous/next months.
- "Insights and correlations" are out of scope for this phase — this spec covers only data definition and daily tracking.
- The onboarding flow appears once for new users (no data types defined yet) and is not shown again once the user has created at least one data type.
- Mock data generation is a developer/demo tool accessible from the app's settings or onboarding — it populates the previous 2 calendar months with randomised daily entries.
- Pre-defined onboarding suggestions include common wellness trackers: period/bleeding, mood, medication taken, sleep quality, exercise, left the house, saw friends, headache, energy level.
- There is no limit on the number of custom data types a user can create, but the UI should remain usable with up to 30 data types.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Define a Custom Data Type (Priority: P1)

As a user, I want to create a new personal data type by choosing an emoji and writing a short description, so that I can track anything meaningful to me.

**Why this priority**: Without data types, there is nothing to track. This is the foundational building block for the entire app.

**Independent Test**: Can be fully tested by opening the "add data type" flow, selecting an emoji, entering a description, saving, and confirming the new data type appears in the user's list.

**Acceptance Scenarios**:

1. **Given** the user has the app open, **When** they tap "Add data type", **Then** they are presented with an emoji selector and a text field for the description.
2. **Given** the user has selected an emoji and typed a description (≤ 60 characters), **When** they tap "Save", **Then** the data type is persisted and visible in their data-type list with the chosen emoji and description.
3. **Given** the user attempts to save without selecting an emoji or without entering a description, **When** they tap "Save", **Then** the app shows an inline validation message indicating what is missing and does not save.
4. **Given** the user already has a data type with the same emoji and description, **When** they attempt to create a duplicate, **Then** the app warns them that this data type already exists and does not create a duplicate.

---

### User Story 2 — Log Daily Data via Calendar (Priority: P1)

As a user, I want to open a calendar, select a day, and toggle which data types apply to that day, so that I can build a daily record over time.

**Why this priority**: Logging daily data is the core interaction loop. Without it the app has no purpose.

**Independent Test**: Can be tested by navigating to the calendar, tapping a day, toggling one or more data types on, and confirming the day shows the selected data types when revisited.

**Acceptance Scenarios**:

1. **Given** the user has at least one data type defined, **When** they open the calendar screen, **Then** they see the current month with today's date highlighted.
2. **Given** the user is viewing the calendar, **When** they tap a day, **Then** a panel or bottom sheet appears showing all their data types with toggles.
3. **Given** the user toggles a data type on for a day and taps "Save" / "Done", **When** they tap the same day again, **Then** that data type is shown as active (toggled on).
4. **Given** the user has logged data for a day, **When** they view the calendar month overview, **Then** days with logged data display a visual indicator (e.g., dot or emoji preview) distinguishing them from empty days.
5. **Given** the user is viewing the calendar, **When** they swipe or tap navigation arrows, **Then** the calendar navigates to the previous or next month with an animated transition.

---

### User Story 3 — Edit or Delete a Data Type (Priority: P2)

As a user, I want to edit the emoji or description of an existing data type, or delete it entirely, so that I can keep my tracking list accurate and relevant.

**Why this priority**: Users will inevitably want to refine or remove data types. This is essential for long-term usability but not required for the very first data entry.

**Independent Test**: Can be tested by long-pressing or tapping an edit action on an existing data type, changing the emoji or description, saving, and confirming the update is reflected everywhere (including historical calendar entries). Deletion can be tested by deleting a data type and confirming it and its historical entries are removed.

**Acceptance Scenarios**:

1. **Given** the user has at least one data type, **When** they tap the edit action on a data type, **Then** they see the current emoji and description pre-filled and can modify either.
2. **Given** the user changes the emoji or description and saves, **When** they view the data-type list or any calendar day that references it, **Then** the updated emoji and description are shown.
3. **Given** the user taps delete on a data type, **When** they confirm the deletion, **Then** the data type and all its associated daily entries are permanently removed.
4. **Given** the user taps delete, **When** the confirmation prompt appears, **Then** they can cancel to keep the data type intact.

---

### User Story 4 — New-User Onboarding (Priority: P2)

As a new user opening the app for the first time, I want to be guided through creating my initial set of data types with helpful suggestions, so that I can start tracking quickly without feeling overwhelmed.

**Why this priority**: A good first-run experience reduces abandonment and helps users understand the app's value immediately. However, a power user could skip onboarding and create data types manually.

**Independent Test**: Can be tested by clearing app data (fresh install state), launching the app, stepping through the onboarding flow, selecting suggested data types, and confirming they appear in the data-type list afterward.

**Acceptance Scenarios**:

1. **Given** the user opens the app for the first time (no data types exist), **When** the app loads, **Then** the onboarding flow starts automatically.
2. **Given** the user is in the onboarding flow, **When** the suggestions screen appears, **Then** they see a set of pre-defined data types (emoji + description) they can select or deselect.
3. **Given** the user has selected one or more suggestions, **When** they tap "Continue" or "Get Started", **Then** the selected data types are created and saved, and the user is taken to the main calendar screen.
4. **Given** the user is in the onboarding flow, **When** they choose to skip or select none, **Then** they proceed to the main calendar screen with no data types, and can add them manually.
5. **Given** the onboarding mentions "you can add more later", **When** the user finishes onboarding, **Then** the path to adding new data types is clearly discoverable from the main screen.

---

### User Story 5 — Generate Mock Data (Priority: P3)

As a user (or demo presenter), I want to generate 2 months of realistic mock data so that I can see what the app looks and feels like when populated with history.

**Why this priority**: Useful for demos and evaluating the calendar's populated state, but not required for real day-to-day usage.

**Independent Test**: Can be tested by triggering mock data generation, then navigating the calendar across the previous 2 months and confirming that entries exist with a realistic distribution.

**Acceptance Scenarios**:

1. **Given** the user has at least one data type defined, **When** they trigger "Generate mock data" from settings, **Then** the app creates randomised daily entries for the previous 2 calendar months.
2. **Given** mock data is being generated, **When** the generation completes, **Then** the user sees a confirmation message and the calendar reflects the new entries.
3. **Given** mock data has already been generated, **When** the user triggers generation again, **Then** the app warns that existing mock data will be overwritten and asks for confirmation.
4. **Given** no data types are defined, **When** the user tries to generate mock data, **Then** they are prompted to create at least one data type first.

---

### Edge Cases

- **Maximum data types**: The UI must remain usable when a user creates up to 30 data types; scrolling or grid layout should accommodate them without clipping or overlap.
- **Long descriptions**: Descriptions exceeding 60 characters are rejected at input time with a character counter visible to the user.
- **Emoji rendering**: If a selected emoji does not render on the device (unsupported Unicode version), the app should display a fallback placeholder (e.g., a generic circle icon).
- **Past/future navigation**: The calendar allows navigating to past months. Future months beyond the current month are navigable but logging data for future dates is permitted (e.g., planning).
- **Calendar boundary**: The calendar should reasonably support at least 12 months of backward navigation.
- **Empty day panel**: If a user taps a day with no data types defined at all, the day panel shows a prompt to create their first data type.
- **Deleting a data type with extensive history**: Deletion removes all associated entries; the confirmation dialog clearly states that historical data will be lost.
- **Mock data with many data types**: Mock generation should randomly toggle each data type per day with a realistic probability (not all-on or all-off every day).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Users MUST be able to create a custom data type by selecting an emoji and entering a text description (max 60 characters).
- **FR-002**: Users MUST be able to view all their defined data types in a scrollable list showing each data type's emoji and description.
- **FR-003**: Users MUST be able to edit the emoji or description of any existing data type, with changes reflected across all historical entries.
- **FR-004**: Users MUST be able to delete a data type, which permanently removes it and all its associated daily entries after confirmation.
- **FR-005**: The app MUST display a monthly calendar view as the primary interface, with the current month shown by default and today's date visually highlighted.
- **FR-006**: Users MUST be able to navigate between months (forward and backward) using swipe gestures or navigation controls.
- **FR-007**: Users MUST be able to tap a calendar day to open a panel showing all defined data types as toggleable items.
- **FR-008**: The app MUST persist toggled data-type states per day when the user taps the "Save" / "Done" button on the day panel, so that re-opening a day's panel shows previously saved selections.
- **FR-009**: Days with at least one active data-type entry MUST display a visual indicator on the calendar (e.g., dot, emoji preview, or colour).
- **FR-010**: The app MUST present an onboarding flow on first launch (when no data types exist) that offers pre-defined data-type suggestions: period/bleeding, mood, medication taken, sleep quality, exercise, left the house, saw friends, headache, energy level.
- **FR-011**: Users MUST be able to skip onboarding and proceed to the main calendar with no data types.
- **FR-012**: The onboarding MUST communicate that additional data types can be created later.
- **FR-013**: Users MUST be able to generate 2 months of randomised mock data for all defined data types from a settings or onboarding action.
- **FR-014**: Mock data generation MUST warn and request confirmation before overwriting previously generated mock data.
- **FR-015**: The app MUST validate that an emoji and a non-empty description are provided before saving a data type, displaying inline error messages on failure.
- **FR-016**: The app MUST prevent creation of duplicate data types (same emoji and same description).
- **FR-017**: All screen transitions, state changes, and interactive elements MUST include purposeful animations consistent with the app's delight and motion design principle.

### Key Entities

- **Data Type**: A user-defined category for tracking. Attributes: unique identifier, emoji (single emoji character), description (text, max 60 characters), creation date, sort order (default: creation date descending — newest first).
- **Daily Entry**: A record linking a specific calendar date to one or more active data types. Attributes: date, reference to data type, active/inactive state.
- **Onboarding Suggestion**: A pre-defined data-type template offered during first-run onboarding. Attributes: emoji, description, selected-by-default flag.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A new user can complete onboarding and log their first daily entry within 2 minutes of opening the app.
- **SC-002**: Creating a new custom data type (emoji selection + description + save) takes fewer than 30 seconds.
- **SC-003**: Navigating between calendar months and opening a day's data panel each complete within 1 second, including animations.
- **SC-004**: The calendar displays populated days with visual indicators, allowing a user to distinguish logged days from empty days at a glance.
- **SC-005**: After generating mock data, the previous 2 months of calendar show realistic, varied daily entries with no month appearing uniformly empty or uniformly full.
- **SC-006**: Users can manage up to 30 data types without the UI becoming unusable (all items visible via scrolling, no clipping or overlap).
- **SC-007**: 90% of first-time users complete onboarding without requiring external guidance (self-explanatory flow).
- **SC-008**: All screen transitions and state changes are animated — no abrupt visual jumps are present in the app.
