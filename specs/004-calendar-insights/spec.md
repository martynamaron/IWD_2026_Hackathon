# Feature Specification: Calendar Insights Panel

**Feature Branch**: `004-calendar-insights`  
**Created**: 13 March 2026  
**Status**: Draft  
**Input**: User description: "On the main Calendar view, I want the bottom part of the screen to be a place where the app adds insights when there is enough data to be analysed. It might take a while to load, so add a nice animated loading progress bar there. The data analysis of the past inputs should focus on correlations and patterns found, i.e. 'You had headaches when you didn't leave the house 80% of the time'. The app should decide which insights and correlations are relevant. Do not provide any medical advice, just spot the patterns that might help the user. Using AI is acceptable to figure out the patterns and parse the data."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Viewing Insights on the Calendar Screen (Priority: P1)

A user opens the Calendar view after having tracked data for several weeks. Below the calendar grid, they see an insights panel that displays automatically generated observations about patterns in their tracked data. Each insight is a plain-language sentence describing a correlation the system found (e.g., "You reported headaches on 80% of the days you didn't leave the house"). The user can scroll through multiple insights if more than one was found.

**Why this priority**: This is the core value of the feature — surfacing useful patterns from tracked data directly on the main screen without requiring the user to seek them out.

**Independent Test**: Can be fully tested by seeding the app with at least 2 weeks of varied daily entries across multiple data types, then opening the Calendar screen and verifying that relevant correlation insights appear in the bottom panel.

**Acceptance Scenarios**:

1. **Given** a user has tracked at least 2 weeks of data across 2 or more data types, **When** they open the Calendar screen, **Then** an insights panel appears in the bottom portion of the screen displaying one or more plain-language correlation statements.
2. **Given** insights are available, **When** the user reads an insight, **Then** the insight describes a pattern or correlation between tracked data types using clear, non-medical language (e.g., "On days you exercised, your mood was rated 4+ out of 5 about 75% of the time").
3. **Given** insights are available, **When** multiple insights are generated, **Then** the user can scroll through them within the insights panel.

---

### User Story 2 - Seeing a Delightful Loading State While Insights Are Generated (Priority: P1)

A user opens the Calendar screen. Because analysing their tracked data takes time (especially when AI processing is involved), they see a custom, visually delightful animated loader in the insights panel area while results are being computed. This is not a standard platform progress bar — it should feel crafted, pleasant, and on-brand with the app's personality (e.g., a pulsing glow, animated dots, a playful shimmer, or a branded motion graphic). The animation should feel smooth and calming, giving the user confidence that something meaningful is happening. Once analysis completes, the loader gracefully transitions to reveal the actual insights.

**Why this priority**: Without a loading state, the bottom of the screen would appear empty or broken during analysis, causing confusion. A delightful, custom loader turns a wait into a positive micro-interaction and reinforces the app's quality feel.

**Independent Test**: Can be fully tested by opening the Calendar screen with enough data and verifying that a custom animated loader (distinct from the standard platform progress bar) appears before insights are displayed, and that it transitions smoothly to the insights content.

**Acceptance Scenarios**:

1. **Given** a user has enough tracked data, **When** they open the Calendar screen and insights are being computed, **Then** a custom, visually delightful animated loader is displayed in the insights panel area — not a default platform progress bar.
2. **Given** insights are loading, **When** the analysis completes, **Then** the loader gracefully animates out and the insights content animates in with a smooth transition.
3. **Given** the user navigates away from the Calendar screen and returns, **When** insights have not changed, **Then** previously computed insights are shown immediately without a loading state (cached results).
4. **Given** the loading animation is playing, **When** the user watches it, **Then** the animation feels smooth, continuous, and pleasant — it should not stutter, freeze, or feel mechanical.

---

### User Story 3 - Handling Insufficient Data (Priority: P2)

A new user or a user who has only tracked data for a few days opens the Calendar screen. Instead of an empty or confusing insights area, they see a friendly message explaining that the app needs more data before it can surface patterns, along with encouragement to keep tracking.

**Why this priority**: Gracefully handling the "no insights yet" state prevents user frustration and sets expectations for when insights will become available.

**Independent Test**: Can be fully tested by opening the Calendar screen on a fresh install or with fewer than 2 weeks of tracked data and verifying the empty-state message appears.

**Acceptance Scenarios**:

1. **Given** a user has tracked data for fewer than 7 days, **When** they open the Calendar screen, **Then** the insights panel shows a friendly message such as "Keep tracking! Insights will appear once there's enough data to find patterns."
2. **Given** a user has data for 7–13 days, **When** they open the Calendar screen, **Then** the system attempts analysis but may show a message indicating that more data will improve insights quality.
3. **Given** a user has no tracked data types at all, **When** they open the Calendar screen, **Then** the insights panel is not shown.

---

### User Story 4 - Insights Are Non-Medical and Pattern-Focused (Priority: P2)

All insights generated by the system are strictly observational — they describe correlations and patterns without offering medical advice, diagnoses, or treatment recommendations. Each insight includes a clear disclaimer where appropriate that correlation does not imply causation.

**Why this priority**: Ensures the app does not cross into medical territory, which would be inappropriate and potentially harmful. The app is a pattern-spotting tool, not a health advisor.

**Independent Test**: Can be tested by reviewing all generated insights to confirm none contain medical advice, diagnoses, or recommendations for treatment, and that the panel includes a general disclaimer.

**Acceptance Scenarios**:

1. **Given** the system generates insights, **When** the user reads them, **Then** no insight contains words like "you should", "we recommend", "diagnosis", "treatment", "consult your doctor", or similar medical advice language.
2. **Given** the insights panel is displayed, **When** the user views it, **Then** a brief disclaimer is visible (e.g., "These are patterns in your data, not medical advice").
3. **Given** the AI generates a potential insight that could be interpreted as medical advice, **When** the system processes it, **Then** it is filtered out or rephrased to be strictly observational.

---

### User Story 5 - Insights Update as New Data Is Tracked (Priority: P3)

When the user logs new daily entries and returns to the Calendar screen, insights are refreshed to incorporate the latest data. The user does not need to manually trigger a refresh.

**Why this priority**: Keeping insights current ensures the feature remains useful over time, but it's a lower priority since insights don't need to update in real-time — they can update on screen load.

**Independent Test**: Can be tested by logging new daily entries, navigating back to the Calendar screen, and verifying that insights reflect the updated data set.

**Acceptance Scenarios**:

1. **Given** the user has logged new data since the last time insights were generated, **When** they navigate to the Calendar screen, **Then** the system re-analyses the data and updates insights accordingly (showing a loading state during re-analysis).
2. **Given** the user has not logged new data, **When** they navigate to the Calendar screen, **Then** previously cached insights are shown without re-analysis.

---

### Edge Cases

- What happens when all tracked data is uniform (e.g., the user toggled "yes" every day for every data type)? The system should recognize there are no meaningful correlations and display a message like "No patterns found yet — try tracking varied data over time."
- What happens when a data type is deleted after insights were generated referencing it? Insights referencing deleted data types should be removed or regenerated.
- What happens when the on-device AI analysis fails or returns an error (e.g., processing failure, out of memory)? The insights panel should display a graceful error message such as "Couldn't generate insights right now. Try again later." and not crash.
- What happens when the user has only one data type tracked? The system should recognize that cross-correlation requires at least 2 data types and inform the user: "Track more than one data type to discover patterns."
- What happens when insights are stale (e.g., computed days ago but user hasn't opened the app)? The system should re-analyse when the Calendar screen is opened if data has changed since last analysis.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Calendar screen MUST display an insights panel in the bottom portion of the screen, below the calendar grid.
- **FR-002**: The insights panel MUST show a custom, visually delightful animated loader (not a standard platform progress bar) while data analysis is in progress. The loader should feel crafted, smooth, and pleasant.
- **FR-003**: The system MUST analyse all tracked daily entries to identify correlations and patterns between different data types.
- **FR-004**: Each insight MUST be presented as a plain-language sentence describing a pattern or correlation found in the data (e.g., "You had headaches on 80% of days when you didn't leave the house").
- **FR-005**: Insights MUST include percentage or frequency information to quantify the strength of the correlation.
- **FR-006**: The system MUST NOT generate or display any medical advice, diagnoses, or treatment recommendations.
- **FR-007**: The insights panel MUST display a small, persistent disclaimer line (at the top or bottom of the panel) indicating that insights are data patterns, not medical advice. This text is always visible when insights are shown — it is non-intrusive but never hidden or dismissible.
- **FR-008**: The system MUST require a minimum data threshold before generating insights — at least 7 days of tracked data across at least 2 data types.
- **FR-009**: When insufficient data exists, the insights panel MUST display a friendly empty-state message encouraging the user to keep tracking.
- **FR-010**: The system MUST persist generated insights to the local database so they survive app restarts and can be displayed instantly on cold launch without re-analysis. Re-analysis occurs only when new data has been logged since the last analysis run.
- **FR-011**: The system MUST re-analyse data when the user opens the Calendar screen if new entries have been logged since the last analysis.
- **FR-012**: The system MUST autonomously determine which correlations are relevant and worth surfacing — the user does not manually configure insights.
- **FR-013**: The system MUST support analysing all existing input types: toggle (presence/absence), scale (0–5 values), and multiple choice selections.
- **FR-014**: The system MUST gracefully handle analysis failures by displaying a user-friendly error message in the insights panel.
- **FR-015**: The system MUST filter out insights where the co-occurrence or correlation strength is below 60%. This threshold balances surfacing useful patterns while avoiding noise from coincidental data.
- **FR-016**: The system MUST use statistical correlation analysis as the primary method for identifying patterns. This ensures fast results on all devices.
- **FR-017**: The system MAY optionally use an on-device LLM (when available on the device) to enhance insight phrasing or detect more complex patterns. Statistical analysis remains the required baseline; the LLM is an additive enhancement, not a prerequisite.
- **FR-018**: All analysis MUST run on-device — no network calls or cloud services.

### Key Entities

- **Insight**: A single pattern or correlation observation. Key attributes: human-readable description, involved data types, correlation strength (percentage/frequency), date range analysed, timestamp of generation. Persisted to local database.
- **Analysis Result**: A collection of insights generated from a single analysis run. Key attributes: list of insights, data snapshot timestamp (used to detect whether re-analysis is needed), analysis status (loading, complete, failed, insufficient data). Persisted to local database.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can see at least one relevant insight within 10 seconds of opening the Calendar screen (when sufficient data exists).
- **SC-002**: 100% of generated insights are free of medical advice, diagnoses, or treatment language.
- **SC-003**: Users with fewer than 7 days of data see a clear empty-state message instead of a blank area or error.
- **SC-004**: The animated loading indicator is visible within 500 milliseconds of opening the Calendar screen when analysis is in progress.
- **SC-005**: Cached insights are displayed instantly (under 1 second) when no new data has been logged since the last analysis.
- **SC-006**: All correlations in generated insights include a quantified metric (percentage or frequency count).
- **SC-007**: Insights cover cross-data-type correlations (e.g., relating headaches to leaving the house), not just single-data-type summaries.

## Assumptions

- The app currently has no AI or network infrastructure. This feature introduces on-device AI-powered analysis as a new capability.
- The primary analysis approach is statistical correlation (co-occurrence, frequency analysis), which is fast and works on all devices. An on-device LLM (e.g., Gemini Nano) may optionally enhance natural-language phrasing or detect deeper patterns when device hardware supports it.
- All processing runs on-device. No cloud services or network calls are used. This preserves the app's fully-offline, privacy-first design.
- The existing data model (daily entries across toggle, scale, and multiple choice types) provides sufficient input for meaningful correlation analysis.
- Privacy is paramount — all data stays on-device. No user data is transmitted externally.
- Insights are recomputed on Calendar screen open (if data changed), not on a background schedule.
- The minimum data threshold of 7 days and 2 data types is a reasonable starting point; it may be refined based on testing.
- The "bottom portion of the screen" refers to a collapsible or scrollable section below the calendar grid — it should not obscure calendar functionality.

## Clarifications

### Session 2026-03-13

- Q: Where should AI-powered analysis run (on-device, cloud, or hybrid)? → A: On-device only — no network calls or cloud services.
- Q: What analysis approach should be prioritised (on-device LLM, statistical, or pure statistical)? → A: Statistical correlation first (fast, all devices), with optional on-device LLM enhancement for natural-language phrasing.
- Q: How should the medical disclaimer be presented (persistent text, one-time banner, or info icon)? → A: Small persistent text at the top or bottom of the insights panel, always visible, non-intrusive.
- Q: What minimum co-occurrence threshold should an insight require to be shown? → A: 60% — balanced threshold that filters noise while still surfacing useful patterns.
- Q: How should generated insights be cached (in-memory or persisted to database)? → A: Persisted to local database — insights survive app restarts, re-analysed only when data changes.
