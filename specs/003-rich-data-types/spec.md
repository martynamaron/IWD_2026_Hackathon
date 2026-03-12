# Feature Specification: Rich Data Input Types

**Feature Branch**: `003-rich-data-types`
**Created**: 2026-03-12
**Status**: Draft
**Input**: User description: "Some of the data types can't just be binary true/false and represented with a toggle. Mood data type for example is more of a scale from 0 to 5, others may be defined by a multiple choice options. Let the user add different types of this data and edit the existing mood into a scale, and exercise data type into multiple choice, with representations being different emojis like a running man, swimming, tennis, dancing."

## Assumptions

- This feature builds on the existing BioGraph app (spec 001-add-track-data) where data types were binary toggles.
- Three input types will be supported: **Toggle** (true/false, existing behaviour), **Scale** (integer range 0–5, fixed), and **Multiple Choice** (a fixed set of labelled emoji options).
- The scale range is always 0–5 and is not configurable by the user. Both 0 and 5 are valid logged values.
- Multiple choice options consist of an emoji + label pair; the user defines these options when creating or editing the data type.
- A minimum of 2 options is required for a multiple choice data type.
- The maximum number of multiple choice options per data type is 10, matching the practical limit for a readable inline selector.
- Multiple Choice is multi-select: any number of the defined options (including all of them) can be active for the same day. A day with zero options selected is treated as unrecorded for that data type.
- For scale data types, the usable range is 0–5 and both 0 and 5 are valid logged scores. "Unrecorded" is a distinct null state representing the absence of any selection; it is not the same as a score of 0. Scale entries where the user makes no selection are stored as null (unrecorded).
- Existing daily entries for mood (previously binary) are migrated as "not recorded" when the type is changed to scale.
- Existing daily entries for exercise (previously binary) are migrated as "not recorded" when the type is changed to multiple choice.
- The two specific retrofits requested (mood → scale 0–5, exercise → multiple choice with emoji sport options) apply only to **existing users** who already have these data types as Toggles. New users installing the app receive Mood (Scale 0–5) and Exercise (Multiple Choice: 🏃 🏊 🎾 💃) directly through onboarding.
- Sport emoji options for exercise are pre-populated suggestions; the user can confirm, remove, or add their own during the edit.
- All other data types remain as toggles unless the user explicitly changes them.
- The calendar indicator on the month view must still function for scale and multiple choice entries (a day with any non-blank value shows the indicator).
- No changes to authentication, storage engine, or sync are in scope for this phase.
- Input type changes are one-way only: a Toggle data type may be changed to Scale or Multiple Choice, but once changed it cannot be reverted to Toggle or switched to the other rich type. The input type is locked after the first change.

## Clarifications

### Session 2026-03-12

- Q: Can users change a data type's input type more than once (e.g., Scale → Toggle, or Multiple Choice → Scale)? → A: One-way only — Toggle → Scale or Toggle → Multiple Choice is allowed; no reversals or cross-type changes between Scale and Multiple Choice.
- Q: On a given day, can a user select more than one option from a Multiple Choice data type? → A: Yes — multi-select; any number of the defined options can be active on the same day.
- Q: Should a scale value of 0 be treated as "unrecorded", or is 0 a valid logged score? → A: 0 is a valid logged score — the scale runs from 0 to N. "Unrecorded" is a distinct null state (no value selected at all).
- Q: Should the onboarding suggestion for Exercise (and Mood) already use the richer input type, or still create them as Toggles for the user to retrofit? → A: Onboarding creates Exercise as Multiple Choice (🏃 🏊 🎾 💃) and Mood as Scale (0–5) from the start; no retrofit step needed for fresh-install users.
- Q: Should the scale selector render as discrete steps or a continuous slider, and is the maximum configurable? → A: Always discrete tappable steps; scale maximum is fixed at 5 (not user-configurable).

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Create a Scale Data Type (Priority: P1)

As a user, I want to create a new data type that is tracked on a 0–5 numeric scale (e.g., mood), so that I can capture nuanced daily feelings rather than a simple yes/no.

**Why this priority**: Scale is the most distinctive new input type; without it, the feature has no value. Mood is the flagship example and the most requested by the user.

**Independent Test**: Can be fully tested by creating a new data type, selecting "Scale" as the input type, saving, then opening the day panel and tapping a step value for today — confirming it persists when the day is reopened.

**Acceptance Scenarios**:

1. **Given** the user opens the "add data type" screen, **When** they choose the input type, **Then** they see three options: Toggle, Scale, and Multiple Choice.
2. **Given** the user selects "Scale", **When** they configure it, **Then** they see the scale is fixed at 0–5 (no maximum input field is present); they still choose an emoji and description.
3. **Given** the user saves a Scale data type, **When** they open a day panel, **Then** the data type is presented as a row of 6 discrete tappable step buttons labelled 0 through 5.
4. **Given** the user selects a value on the scale and taps "Save", **When** they reopen the same day's panel, **Then** the previously selected scale value is shown.
5. **Given** the user makes no selection on a scale item today and taps "Save", **When** they reopen the day, **Then** the scale shows no value selected (null/unrecorded state — this is visually distinct from a selected value of 0).
6. **Given** the user selects 0 on the scale and taps "Save", **When** they reopen the day, **Then** the scale shows 0 as the actively selected value, visually distinguishable from the unrecorded (null) state.

---

### User Story 2 — Create a Multiple Choice Data Type (Priority: P1)

As a user, I want to create a data type where I can select one or more options from a set of emoji-labelled choices on a given day (e.g., exercise type: 🏃 running, 🏊 swimming, 🎾 tennis, 💃 dancing), so that I can log richer categorical data including multiple activities on the same day.

**Why this priority**: Multiple choice is the second core input type. Exercise is the flagship example. Without this story the new types are incomplete.

**Independent Test**: Can be tested by creating a "Multiple Choice" data type with at least 2 emoji+label options, saving it, opening a day panel, selecting two options, saving, and confirming both selections are shown highlighted next time the day is opened.

**Acceptance Scenarios**:

1. **Given** the user selects "Multiple Choice" as the input type, **When** they configure it, **Then** they can add option entries, each consisting of an emoji and a label (max 30 characters per label).
2. **Given** the user adds options, **When** they attempt to save with fewer than 2 options, **Then** the app shows a validation message and does not save.
3. **Given** the user saves a Multiple Choice data type, **When** they open a day panel, **Then** the data type is shown as a row of tappable emoji chips representing each option.
4. **Given** the user taps one or more emoji chips and taps "Save", **When** they reopen the same day, **Then** all previously selected options are highlighted.
5. **Given** the user makes no selection for a multiple choice item and taps "Save", **When** they reopen the day, **Then** no option is highlighted (unrecorded for that day).
6. **Given** the user had previously selected an option on a day, **When** they tap the same already-selected option, **Then** it is deselected (toggled off); other selected options remain unchanged.
7. **Given** the user selects all available options for a multiple choice data type in one day, **When** they tap "Save", **Then** all options are persisted as selected and shown highlighted on revisit.

---

### User Story 3 — Retrofit Mood to Scale (Priority: P2)

As a user, I want to change the existing "Mood" data type (currently a toggle) into a 0–5 scale, so that I can express the nuance of my daily mood going forward without losing the data type's history.

**Why this priority**: This is an edit to an existing data type. It builds on the scale creation story and has higher priority than exercise retrofit because mood is a more universally used tracker.

**Independent Test**: Can be tested by opening the edit screen for the Mood data type (existing-user path), changing its input type to Scale with a maximum of 5, saving, and confirming the day panel now shows a scale selector instead of a toggle. Historical entries for mood should be migrated to "not recorded". For fresh-install users, Mood already appears as Scale after onboarding.

**Acceptance Scenarios**:

1. **Given** the user opens the edit screen for an existing Toggle data type, **When** they change the input type, **Then** they can switch to Scale or Multiple Choice and configure the new settings.
2. **Given** the user switches an existing Toggle type to Scale, **When** they confirm the edit, **Then** the app warns that historical binary entries for this data type will be reset to "not recorded" and asks for confirmation.
3. **Given** the user confirms the type change, **When** they revisit days that had this data type toggled on, **Then** the scale shows no value selected (not recorded) for those days.
4. **Given** the existing Mood data type is selected, **When** the user edits it, **Then** Scale (max 5) is shown as the recommended configuration with the option pre-filled.

---

### User Story 4 — Retrofit Exercise to Multiple Choice (Priority: P2)

As a user, I want to change the existing "Exercise" data type into a multiple choice type with sport emojis (🏃 running, 🏊 swimming, 🎾 tennis, 💃 dancing), so that I can log which activity I did each day.

**Why this priority**: Same motivation as mood retrofit — adds real value to an existing data type. Slightly lower than mood as fewer users track specific sport types, but still explicitly requested.

**Independent Test**: Can be tested by editing the Exercise data type (existing-user path), switching to Multiple Choice, reviewing the pre-populated sport emoji suggestions, saving, and verifying the day panel shows emoji chips. Historical entries should show as not recorded. For fresh-install users, Exercise already appears as Multiple Choice after onboarding.

**Acceptance Scenarios**:

1. **Given** the user edits the Exercise data type, **When** they select Multiple Choice, **Then** the app pre-populates suggestions: 🏃 Running, 🏊 Swimming, 🎾 Tennis, 💃 Dancing.
2. **Given** the pre-populated options are shown, **When** the user reviews them, **Then** they can remove any suggestion or add additional emoji+label options before saving.
3. **Given** the user saves the multiple choice configuration, **When** they revisit days that had Exercise toggled on, **Then** the multiple choice selector shows no option selected (migrated to not recorded).
4. **Given** the user saves the multiple choice configuration, **When** they open today's day panel, **Then** Exercise is shown with four (or however many remain) tappable emoji chips.

---

### User Story 5 — Create New Data Type with Any Input Type (Priority: P3)

As a user, I want to freely choose Toggle, Scale, or Multiple Choice when creating any new data type, so that my future tracking is not limited to yes/no.

**Why this priority**: This generalises the above stories to all future data types. It is low priority because the core validated with mood and exercise; general creation is additive.

**Independent Test**: Can be tested by creating three new data types (one of each input type), logging values on the same day, and confirming all three persist with correct values on revisit.

**Acceptance Scenarios**:

1. **Given** the user opens "Add data type", **When** they step through creation, **Then** they must choose an input type (Toggle, Scale, Multiple Choice) before saving.
2. **Given** the user selects Toggle, **When** they save, **Then** the data type behaves exactly as data types did before this feature.
3. **Given** the user selects Scale, **When** they save, **Then** the data type is immediately usable with a fixed 0–5 step selector in the day panel; no maximum configuration step is shown.
4. **Given** the user selects Multiple Choice and adds options, **When** they save, **Then** each option including its emoji and label is persisted and shown in the day panel.

---

### Edge Cases

- **Type change with extensive history**: Changing a data type from Toggle to Scale or Multiple Choice resets all historical entries for that type to "not recorded". The warning dialog must state the number of historical entries that will be affected.
- **Multiple choice with duplicate options**: If a user adds two options with the same emoji and label, the app rejects the duplicate with an inline validation message.
- **Scale maximum of 1**: A scale with maximum 1 is effectively binary; the app should allow it but display it as two discrete steps (0 and 1) rather than a slider.
- **Scale value on calendar indicator**: Days where a scale value has been explicitly set (any integer 0–5) show the data type's emoji in the calendar indicator. Days with a null (unrecorded) state do not.
- **Multiple choice value on calendar indicator**: Days where one or more options have been selected show the data type's emoji in the calendar indicator; a day with zero options selected does not.
- **Editing options on an existing multiple choice type**: If the user removes an option that was selected on a previous day, those past entries are migrated to "not recorded" for that data type.
- **Maximum options reached**: When a multiple choice data type already has 10 options, the "Add option" button is disabled with a tooltip explaining the limit.
- **Empty label**: An option with an emoji but an empty label is rejected with an inline validation message.
- **Scale value on calendar indicator**: Days where a scale value has been explicitly set (any integer 0–5) show the data type's emoji in the calendar indicator. Days with a null (unrecorded) state do not.
- **Multiple choice value on calendar indicator**: Days where one or more options have been selected show the data type's emoji in the calendar indicator; a day with zero options selected does not.
- **Editing options on an existing multiple choice type**: If the user removes an option that was selected on a previous day, those past entries are migrated to "not recorded" for that data type.
- **Empty label**: An option with an emoji but an empty label is rejected with an inline validation message.
- **Scale maximum > 10**: The app allows it but warns the user that a large scale may be hard to interact with on a small screen.
- **Re-editing a non-Toggle type**: When the user opens the edit screen for a Scale or Multiple Choice data type, the input type field is shown as read-only or hidden; no option to change the type is present, preventing accidental migrations or re-migrations.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: When creating or editing a data type, users MUST be able to select one of three input types: Toggle, Scale, or Multiple Choice.
- **FR-002**: Scale data types MUST have a fixed range of 0–5; users MUST NOT be able to configure the maximum value. No maximum input field is shown when creating a Scale data type.
- **FR-003**: Users MUST be able to configure a Multiple Choice data type by defining between 2 and 10 options, each consisting of an emoji and a label (max 30 characters).
- **FR-004**: The day panel MUST render Toggle data types as a binary switch (existing behaviour), Scale data types as a row of 6 discrete tappable step buttons labelled 0 through 5 (no slider), and Multiple Choice data types as a row of tappable emoji chips.
- **FR-005**: The app MUST persist the selected scale value (an integer in the range 0–5, including 0) or a null/unrecorded state per Scale data type per day when the user taps "Save". A stored value of 0 MUST be visually distinct from an unrecorded (null) state in the day panel.
- **FR-006**: The app MUST persist all selected multiple choice options (zero or more) per data type per day when the user taps "Save". A day with zero options selected is treated as unrecorded for that data type.
- **FR-007**: Users MUST be able to individually toggle each multiple choice option on or off for a given day; tapping a selected option deselects it without affecting other selections.
- **FR-008**: Users MUST be able to change the input type of an existing **Toggle** data type to Scale or Multiple Choice via the edit screen. This change is permanent — once a data type has been changed from Toggle, its input type MUST be read-only and cannot be changed again. The input type selector MUST be absent or disabled for Scale and Multiple Choice data types.
- **FR-009**: When changing an existing data type's input type, the app MUST warn the user that historical entries will be reset to unrecorded and display the count of affected entries, requiring explicit confirmation before proceeding.
- **FR-010**: After confirming a type change, the app MUST migrate all historical daily entries for that data type to an unrecorded state.
- **FR-011**: The edit screen for the Mood data type MUST pre-fill Scale (max 5) as the suggested new input type configuration.
- **FR-012**: The edit screen for the Exercise data type MUST pre-populate Multiple Choice with four suggestions: 🏃 Running, 🏊 Swimming, 🎾 Tennis, 💃 Dancing.
- **FR-013**: Users MUST be able to remove or add options to the pre-populated exercise suggestions before saving.
- **FR-014**: The calendar month view indicator for a day MUST reflect any non-blank value for Scale or Multiple Choice data types (same as existing toggle behaviour: any active entry triggers the indicator).
- **FR-015**: The app MUST validate that multiple choice options do not contain duplicate emoji+label combinations, showing an inline error on failure.
- **FR-016**: The app MUST validate that at least 2 options exist when saving a Multiple Choice data type, showing an inline error if the requirement is not met.
- **FR-017**: An emoji and a non-empty label are required for each multiple choice option; the app MUST display inline validation messages if either is missing.
- **FR-018**: All new interaction patterns (scale step selector, emoji chip selection) MUST include purposeful animations consistent with the app's existing motion design.
- **FR-019**: The onboarding flow MUST create the "Mood" suggestion as a Scale data type (max 5) and the "Exercise" suggestion as a Multiple Choice data type (options: 🏃 Running, 🏊 Swimming, 🎾 Tennis, 💃 Dancing) when the user selects these suggestions during first-run onboarding. All other onboarding suggestions remain as Toggle data types.

### Key Entities

- **Data Type** *(updated)*: Gains an `inputType` attribute (Toggle | Scale | MultipleChoice) and an optional `options` list (used only for MultipleChoice). Scale data types have no configurable maximum — the range 0–5 is fixed and requires no additional attribute.
- **Onboarding Suggestion** *(updated)*: Gains an `inputType` field so that individual suggestions can specify Toggle, Scale, or Multiple Choice. The Mood suggestion carries `inputType: Scale` (0–5 fixed); the Exercise suggestion carries `inputType: MultipleChoice` with the four sport emoji options pre-attached.
- **Daily Entry** *(updated)*: The value field expands to accommodate three states: boolean (Toggle), integer-or-null (Scale — integer range 0–5 is a valid logged score; null means unrecorded/no selection made), set-of-option-references (MultipleChoice — empty set means unrecorded). Previous binary active/inactive field is retained for Toggle types only.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can change an existing Toggle data type to Scale or Multiple Choice in under 60 seconds, including reviewing and confirming the migration warning.
- **SC-002**: Users can create a new Multiple Choice data type with all four sport options in under 90 seconds from opening the "Add data type" screen to seeing it in the day panel.
- **SC-003**: A scale discrete-step selector or multiple choice chip row for a day is fully operable (value set and saved) in under 10 seconds per data type.
- **SC-004**: After migrating Mood to Scale (0–5), 100% of previously binary Mood entries are shown as unrecorded — no data corruption or silent carry-over of boolean values.
- **SC-005**: The calendar month indicator correctly reflects Scale and Multiple Choice entries: days with a recorded value show the indicator; days without do not, with 100% accuracy across migrated and newly created entries.
- **SC-006**: Users can manage up to 10 multiple choice options per data type without the option list or the day panel chip row becoming unusable (all options visible, no clipping).
- **SC-007**: A fresh-install user who selects both Mood and Exercise during onboarding immediately sees Mood as a Scale selector and Exercise as an emoji chip row in the day panel — no additional configuration or retrofit steps required.

