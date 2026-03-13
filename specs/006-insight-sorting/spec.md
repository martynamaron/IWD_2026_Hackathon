# Feature Specification: Insight Sorting

**Feature Branch**: `006-insight-sorting`  
**Created**: 13 March 2026  
**Status**: Draft  
**Input**: User description: "I want the user to be able to sort the insights two ways - by individual data type, or how strong the correlation between two data types is. Make the UI intuitive"

## Clarifications

### Session 2026-03-13

- Q: Should data type groups in the "By Data Type" view be collapsible/expandable or always fully expanded? → A: Collapsible — groups start expanded, user can tap a header to collapse/expand.
- Q: Should strength accent colours be fixed values, semantic theme tokens, or opacity variants of a single brand colour? → A: Use semantic colour tokens from the app's theme so colours adapt automatically if the brand theme changes.
- Q: How should the sort toggle coexist with the time period filter tabs from spec 004? → A: Stacked layout — time period tabs on the top row, sort toggle on a second row directly below.
- Q: Should duplicate insight cards in the "By Data Type" view indicate they also appear in another group? → A: Yes — show a subtle "Also in: [other data type]" tag on each duplicated insight card.
- Q: Should the spec explicitly require accessibility support for the new interactive controls? → A: No — accessibility is deferred for now.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Sorting Insights by Correlation Strength (Priority: P1)

A user opens the Calendar screen and sees the insights panel populated with several correlation insights. By default, insights are sorted by correlation strength — the strongest correlations appear first. The user can see at a glance which patterns in their data are the most significant. Each insight visually indicates its correlation strength (e.g., a label like "Strong", "Moderate", or a percentage), making it immediately clear why insights are ordered the way they are.

**Why this priority**: Sorting by correlation strength is the highest-value default because it surfaces the most meaningful patterns first. Users who just want quick, high-confidence takeaways get them without any interaction.

**Independent Test**: Can be fully tested by having a dataset with insights of varying correlation strengths, opening the insights panel, and verifying that insights appear in descending order of correlation strength with visible strength indicators.

**Acceptance Scenarios**:

1. **Given** the insights panel contains multiple insights with different correlation strengths, **When** the user views the panel with "By Strength" sort active (the default), **Then** insights are listed in descending order of correlation strength — strongest first.
2. **Given** insights are sorted by strength, **When** the user reads an insight, **Then** a visual strength indicator is visible alongside the insight (e.g., a label such as "Strong", "Moderate", or "Mild", or a percentage badge).
3. **Given** two insights have identical correlation strength, **When** they are displayed, **Then** they are sub-sorted by most recently generated first.

---

### User Story 2 - Sorting Insights by Data Type (Priority: P1)

A user wants to focus on patterns related to a specific aspect of their life they are tracking — for example, "Headache" or "Exercise". They switch to the "By Data Type" sort mode using a toggle control at the top of the insights panel. The insights are now grouped under headers for each tracked data type. Under each header, all insights involving that data type are listed (sorted by strength within each group). The user can quickly scan to the data type they care about and see all related correlations.

**Why this priority**: Equally critical to the strength sort because it lets users answer the question "What affects my headaches?" or "What correlates with my mood?" — a very natural way to explore personal data.

**Independent Test**: Can be fully tested by having insights that span multiple data types, switching to "By Data Type" sort, and verifying that insights are grouped under the correct data type headers with correct within-group ordering.

**Acceptance Scenarios**:

1. **Given** the insights panel is visible, **When** the user selects the "By Data Type" sort option, **Then** insights are grouped under collapsible section headers — one for each tracked data type that has at least one insight. All groups are expanded by default.
2. **Given** insights are grouped by data type, **When** the user looks at a group, **Then** the group header shows the data type name, the count of insights in that group, and a collapse/expand affordance.
3. **Given** a data type group is expanded, **When** the user taps the group header, **Then** the group collapses smoothly, hiding its insights. Tapping again expands it.
4. **Given** an insight involves two data types (e.g., "Headache" and "Stayed indoors"), **When** insights are grouped by data type, **Then** that insight appears under both relevant data type groups so users can find it from either angle.
5. **Given** an insight appears under two data type groups, **When** the user views it in one group, **Then** the insight card displays a subtle "Also in: [other data type]" tag indicating its cross-reference.
6. **Given** insights are grouped by data type, **When** the user looks within a single group, **Then** insights within that group are sorted by correlation strength (strongest first).

---

### User Story 3 - Switching Between Sort Modes (Priority: P1)

The user sees a simple, clearly labelled toggle control at the top of the insights panel that lets them switch between "By Strength" and "By Data Type" sort modes. The toggle is always visible when insights are available. Switching is instant and the layout transitions smoothly between the two views. The user's last selected sort preference is remembered across sessions.

**Why this priority**: The toggle is the mechanism that enables both sort modes; without an intuitive, accessible control, the feature has no way to be used.

**Independent Test**: Can be fully tested by opening the insights panel, tapping the toggle to switch between sort modes, verifying the list re-arranges correctly, closing and reopening the app, and confirming the last-used sort mode persists.

**Acceptance Scenarios**:

1. **Given** the insights panel has at least one insight, **When** the user sees the panel, **Then** a segmented toggle control is visible on a row directly below the time period filter tabs, with two options: "By Strength" and "By Data Type".
2. **Given** the toggle is set to "By Strength", **When** the user taps "By Data Type", **Then** the insights list smoothly transitions to the grouped-by-data-type layout.
3. **Given** the toggle is set to "By Data Type", **When** the user taps "By Strength", **Then** the insights list smoothly transitions to the flat, strength-ordered layout.
4. **Given** the user selects "By Data Type" and closes the app, **When** they reopen the app and navigate to the insights panel, **Then** the toggle is still set to "By Data Type".

---

### User Story 4 - Understanding Correlation Strength at a Glance (Priority: P2)

Each insight card displays a visual indicator of correlation strength so the user can quickly assess significance without reading the full sentence. The strength indicator uses a combination of a label ("Strong", "Moderate", "Mild") and a subtle colour accent to communicate at a glance. This works in both sort modes.

**Why this priority**: Visual strength indicators enhance scannability and make both sort modes more useful, but the feature is functional (via percentage in the text) without them.

**Independent Test**: Can be tested by generating insights with different strength levels and verifying that each displays the correct strength label and colour accent.

**Acceptance Scenarios**:

1. **Given** an insight has a correlation strength of 80% or above, **When** displayed, **Then** it shows a "Strong" label with a visually distinct accent.
2. **Given** an insight has a correlation strength between 60% and 79%, **When** displayed, **Then** it shows a "Moderate" label with a visually distinct accent.
3. **Given** an insight has a correlation strength below 60% (if edge-case displayed), **When** displayed, **Then** it shows a "Mild" label with a visually distinct accent.
4. **Given** the user is in either sort mode, **When** they view any insight, **Then** the strength indicator is consistently displayed in the same position on the insight card.

---

### Edge Cases

- What happens when all insights have the same correlation strength? They should be sub-sorted by recency (most recently generated first), and the strength sort still functions — the list simply appears uniform.
- What happens when there are insights for only one data type? The "By Data Type" view shows a single group. The toggle remains available but a subtle hint may indicate that more data types will create more groups.
- What happens when there are no insights at all (insufficient data)? The sort toggle is hidden and the standard empty-state message is shown. The toggle only appears when at least one insight exists.
- What happens when a data type is deleted after insights referencing it exist? Insights referencing the deleted data type are removed from the panel. If a group becomes empty, it is removed from the "By Data Type" view.
- What happens when insights are loading? The sort toggle is hidden during the loading state. It appears once insights are ready to display.
- What happens when the user switches sort mode while the time period filter (from the calendar insights feature) is also active? The sort mode applies to whatever subset of insights is currently visible for the selected time period. Both controls work independently.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The insights panel MUST provide a segmented toggle control on a dedicated row below the existing time period filter tabs, with two options: "By Strength" and "By Data Type". The layout is stacked: time period tabs on the top row, sort toggle on the second row.
- **FR-002**: The "By Strength" sort MUST display insights in a flat list ordered by correlation strength, descending (strongest first).
- **FR-003**: The "By Data Type" sort MUST group insights under collapsible section headers — one header per tracked data type that appears in at least one insight. All groups MUST be expanded by default.
- **FR-004**: In the "By Data Type" view, an insight that involves two data types MUST appear under both data type groups. Each duplicated card MUST display a subtle cross-reference tag (e.g., "Also in: [other data type name]") so users understand the insight appears in more than one group.
- **FR-005**: In the "By Data Type" view, each group header MUST display the data type name, the number of insights in that group, and a collapse/expand affordance.
- **FR-006**: Within each data type group, insights MUST be sorted by correlation strength (descending).
- **FR-007**: The default sort mode MUST be "By Strength".
- **FR-008**: The user's selected sort mode MUST be persisted locally so it is remembered across app sessions.
- **FR-009**: Each insight MUST display a visual strength indicator consisting of a label ("Strong" for 80%+, "Moderate" for 60–79%, "Mild" for below 60%) and a corresponding colour accent. The accent colours MUST be drawn from the app's semantic colour tokens (not hardcoded values) so they adapt automatically to any brand theme changes.
- **FR-010**: Switching between sort modes MUST animate the transition smoothly — no abrupt layout jumps.
- **FR-011**: The sort toggle MUST NOT be visible when there are no insights to display (empty state, loading state, or error state).
- **FR-012**: When two insights have the same correlation strength, they MUST be sub-sorted by generation timestamp (most recent first).
- **FR-013**: The sort toggle MUST work independently of the existing time period filter — sorting applies to whichever insights are currently visible for the selected time range.
- **FR-014**: Data type group headers in the "By Data Type" view MUST be ordered alphabetically by data type name.
- **FR-015**: Tapping a data type group header MUST toggle that group between collapsed and expanded states with a smooth animation. Collapse/expand state is not persisted across sessions — groups always start expanded.

### Key Entities

- **Sort Mode Preference**: The user's currently selected sort mode ("By Strength" or "By Data Type"). Persisted locally across sessions. One value per user.
- **Strength Tier**: A derived classification of an insight's correlation strength — "Strong" (80%+), "Moderate" (60–79%), "Mild" (below 60%). Used for visual labelling; not stored separately, computed from the insight's correlation percentage.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can switch between the two sort modes with a single tap, and the list re-arranges within 300 milliseconds.
- **SC-002**: 90% of users who interact with the insights panel discover and use the sort toggle within their first session.
- **SC-003**: Users can locate all insights related to a specific data type within 5 seconds when using the "By Data Type" sort.
- **SC-004**: The user's sort preference persists correctly across 100% of app restarts.
- **SC-005**: Every insight displays a correct strength indicator label that matches its correlation percentage.

## Assumptions

- The insights panel and its correlation data already exist as defined in the Calendar Insights feature (spec 004). This feature builds on top of that existing panel.
- Insights already contain a correlation strength percentage (as specified in FR-005 and FR-015 of spec 004). This feature uses that existing data for sorting and labelling.
- The "Mild" tier (below 60%) would only appear if the correlation threshold from spec 004 (FR-015) is adjusted in the future. Currently, all displayed insights are at least 60% and would be "Moderate" or "Strong".
