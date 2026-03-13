# Feature Specification: Mock Data Generation & Insight Trends

**Feature Branch**: `007-mock-data-trends`  
**Created**: 13 March 2026  
**Status**: Draft  
**Input**: User description: "Update mock data generation with configurable time ranges, realistic health correlations, and trend indicators in insights panel"

## Clarifications

### Session 2026-03-13

- Q: When mock data generation is triggered but tracking data already exists, what should happen? → A: Replace all existing data silently (clear and regenerate with no confirmation dialog).
- Q: How should the viewed time range be split into sub-periods for trend comparison? → A: Split into two equal halves (first half vs. second half of the viewed range).
- Q: What minimum difference in correlation strength between the two halves should trigger a non-Stable trend label? → A: ≥15 percentage points difference.
- Q: What visual form should the trend indicator take on the insight card? → A: Small directional arrow icon paired with a text label (e.g., "↑ Strengthening", "↓ Weakening", "→ Stable").

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Generating Realistic Mock Data (Priority: P1)

A new user launches the app for the first time and is offered the option to populate it with sample data so they can explore the app's features. They are presented with two choices: generate 3 months of historical data or 6 months. The user picks one option, and the app fills in daily tracking entries across six data types — Health, Medication, Mood, Energy Levels, Exercise, and Period Bleeding — going back the chosen number of months. The data is realistic: it contains recognisable patterns and correlations that mirror real-life health tracking, giving the user a meaningful demo experience.

**Why this priority**: Without the mock data generation itself, none of the other stories (correlations, trends) can function. This is the foundational data layer everything else depends on.

**Independent Test**: Can be fully tested by selecting each time range option and verifying that entries are created for every day in the chosen range across all six data types, with values that are not purely random (i.e., visible patterns exist alongside natural variation).

**Acceptance Scenarios**:

1. **Given** the user triggers mock data generation, **When** they are presented with time range options, **Then** they see exactly two choices: "3 months" and "6 months".
2. **Given** the user selects "3 months", **When** data generation completes, **Then** entries exist for approximately 90 days back from today across all six data types.
3. **Given** the user selects "6 months", **When** data generation completes, **Then** entries exist for approximately 180 days back from today across all six data types.
4. **Given** mock data has been generated, **When** the user views the calendar, **Then** each day shows tracking entries that vary naturally (not every day is identical).

---

### User Story 2 - Exploring Health Correlations in Mock Data (Priority: P1)

After generating mock data, the user navigates to the insights panel and sees meaningful correlations between their tracked data types. They notice patterns such as: feeling dizzy correlates with missed medication days, mood improves after sustained medication use, and tiredness appears around exercise days (though less so in recent data). These correlations feel realistic and demonstrate the app's analytical capabilities.

**Why this priority**: The core value proposition of the app is surfacing health patterns. If the mock data doesn't contain meaningful, discoverable correlations, the demo experience fails to showcase the product.

**Independent Test**: Can be fully tested by generating mock data, opening the insights panel for different time periods, and verifying that the expected correlations appear with appropriate strength labels and directionality.

**Acceptance Scenarios**:

1. **Given** 6 months of mock data is generated, **When** the user views insights, **Then** a correlation between missed medication (multiple consecutive days without medication) and feeling dizzy is visible.
2. **Given** 6 months of mock data is generated, **When** the user views insights for a period starting after the first 2 months, **Then** a positive correlation between consistent medication use and better mood is visible.
3. **Given** mock data is generated, **When** the user views insights, **Then** period bleeding entries follow a regular cycle of approximately 28 days with bleeding lasting approximately 4–6 days.
4. **Given** mock data is generated, **When** the user views insights, **Then** low mood and low energy levels correlate with the days immediately before and at the start of period bleeding.
5. **Given** mock data is generated, **When** the user views insights, **Then** a correlation between tiredness and exercise days is visible.

---

### User Story 3 - Viewing Trend Indicators on Insights (Priority: P1)

The user switches to the "Last 3 Months" or "Last Year" time period in the insights panel. For correlations that have changed in strength over time, a trend indicator appears on the insight card — showing whether the correlation has gotten stronger, weaker, or remained stable. For example, the user sees that the tiredness–exercise correlation has a "Weakening" trend, suggesting their body is adapting. The user reads the trend annotation alongside the insight text to understand not just current patterns but how those patterns are evolving.

**Why this priority**: Trends add a temporal dimension to insights that transforms static "X correlates with Y" into actionable "X used to correlate with Y but that relationship is changing". This is a key differentiator for helping users understand their health trajectory.

**Independent Test**: Can be fully tested by generating 6 months of mock data, switching to "Last 3 Months" or "Last Year" view, and verifying that at least one insight shows a trend indicator (e.g., the tiredness–exercise correlation shows "Weakening").

**Acceptance Scenarios**:

1. **Given** the user views insights for "Last 3 Months" or "Last Year", **When** a correlation's strength has meaningfully changed over the covered period, **Then** a trend indicator is displayed on that insight card.
2. **Given** a trend indicator is displayed, **When** the user reads it, **Then** it clearly communicates the direction of change: "Strengthening", "Weakening", or "Stable".
3. **Given** the tiredness–exercise correlation weakens over time in the mock data, **When** the user views the "Last 3 Months" or "Last Year" insights, **Then** this specific insight shows a "Weakening" trend indicator.
4. **Given** the user views insights for "Last Month", **When** insights are displayed, **Then** no trend indicators appear (the time window is too short to assess trends).
5. **Given** a correlation has existed for the entire viewed period but has not meaningfully changed in strength, **When** the user views it, **Then** either no trend indicator appears or a "Stable" indicator is shown.

---

### User Story 4 - Updated Data Types in Mock Data (Priority: P1)

The mock data generator creates entries using the updated set of data types that reflect a health-focused tracking use case. The six data types are: Health (multiple choice with options for headache, stomachache, feeling dizzy, vomiting, muscle aches, tiredness), Medication (boolean toggle), Mood (scale), Energy Levels (scale), Exercise (multiple choice with options for long walk, swimming, dancing, tennis), and Period Bleeding (boolean toggle). These replace the previously used demo data types.

**Why this priority**: The data types define what is tracked and are required by all other stories. The correlations and trends depend on these specific data types existing.

**Independent Test**: Can be fully tested by generating mock data and verifying that exactly six data types are created with the correct names, input types, and options.

**Acceptance Scenarios**:

1. **Given** mock data generation is triggered, **When** data types are created, **Then** exactly six data types exist: Health, Medication, Mood, Energy Levels, Exercise, Period Bleeding.
2. **Given** the Health data type is created, **When** the user views its options, **Then** it is a multiple-choice type with options: headache, stomachache, feeling dizzy, vomiting, muscle aches, tiredness.
3. **Given** the Exercise data type is created, **When** the user views its options, **Then** it is a multiple-choice type with options: long walk, swimming, dancing, tennis.
4. **Given** Medication and Period Bleeding data types are created, **When** the user views them, **Then** they are boolean toggle types.
5. **Given** Mood and Energy Levels data types are created, **When** the user views them, **Then** they are scale types.

---

### Edge Cases

- What happens if the user generates mock data when data already exists? The system silently replaces all existing data — it clears previous entries and regenerates from scratch with no confirmation dialog.
- What happens when the 3-month option is selected but trend detection needs longer history? Trend indicators should only appear when there is sufficient data in the viewed time period to detect a meaningful change — at least 2 sub-periods to compare.
- What happens if a menstrual cycle would extend beyond the generated date range? The cycle should be truncated at the boundaries of the date range (first day or today).
- How are "several days of missed medication" defined for the dizziness correlation? A streak of 3 or more consecutive days without a medication entry should trigger elevated probability of feeling dizzy.
- What happens if the user generates 3 months of data and then switches to the "Last Year" view? Only 3 months of data exists, so insights for the "Last Year" view should still compute correctly with whatever data is available, and trend indicators should reflect only the available data range.

## Requirements *(mandatory)*

### Functional Requirements

#### Mock Data Generation

- **FR-001**: The system MUST offer the user exactly two mock data generation options: "3 months" and "6 months" of historical data.
- **FR-002**: When a time range is selected, the system MUST generate daily tracking entries from today back to the start of the chosen period for all six data types.
- **FR-003**: The mock data generator MUST create the following six data types with their corresponding input types:
  - **Health** — multiple choice: headache, stomachache, feeling dizzy, vomiting, muscle aches, tiredness
  - **Medication** — boolean toggle
  - **Mood** — scale
  - **Energy Levels** — scale
  - **Exercise** — multiple choice: long walk, swimming, dancing, tennis
  - **Period Bleeding** — boolean toggle
- **FR-004**: Not every data type needs an entry on every day. The generator MUST introduce natural variation — some days may have no entry for certain types, reflecting realistic tracking behaviour.
- **FR-004a**: When mock data generation is triggered and tracking data already exists, the system MUST silently clear all existing entries and data types, then regenerate from scratch. No confirmation dialog is shown.

#### Correlation Patterns

- **FR-005**: The generator MUST produce a correlation between missed medication and feeling dizzy. Specifically, when medication is not taken for 3 or more consecutive days, the probability of "feeling dizzy" appearing in Health entries MUST increase significantly for the duration of the streak and the 1–2 days following.
- **FR-006**: The generator MUST produce a positive correlation between consistent medication use and improved mood. After approximately 2 months of consistent daily medication entries, mood scale values MUST trend noticeably higher compared to the earlier period.
- **FR-007**: The generator MUST produce period bleeding entries that follow a regular menstrual cycle of approximately 28 days (with minor natural variation of ±2 days), with each bleeding episode lasting approximately 4–6 days.
- **FR-008**: The generator MUST produce lower mood scores and lower energy level scores on days in the 2–3 days immediately before period bleeding begins and during the first 1–2 days of bleeding.
- **FR-009**: The generator MUST produce a correlation between exercise and tiredness. On days when exercise is recorded, tiredness MUST appear in Health entries at a higher rate than on non-exercise days.
- **FR-010**: The exercise–tiredness correlation MUST weaken over time. In the earliest generated data, the correlation should be strong; in the most recent data, it should be noticeably weaker, simulating the user's body adapting to exercise.

#### Trend Indicators

- **FR-011**: For the "Last 3 Months" and "Last Year" time period views, the system MUST compute trend indicators for each insight by splitting the viewed range into two equal halves and comparing the correlation strength of the first half against the second half.
- **FR-012**: Each trend indicator MUST classify the change as one of: "Strengthening" (correlation getting stronger over time), "Weakening" (correlation getting weaker over time), or "Stable" (no meaningful change).
- **FR-013**: Trend indicators MUST be displayed on the insight card alongside the existing insight text and strength label. Each indicator MUST consist of a small directional arrow icon paired with a text label: "↑ Strengthening", "↓ Weakening", or "→ Stable".
- **FR-014**: Trend indicators MUST NOT appear for the "Last Month" time period view, as the window is too short to assess meaningful trends.
- **FR-015**: A trend MUST only be classified as "Strengthening" or "Weakening" when the difference in correlation strength between the first and second halves is ≥15 percentage points. Differences below this threshold MUST be classified as "Stable" or show no trend indicator.

### Key Entities

- **Mock Data Configuration**: The user's choice of time range for data generation (3 months or 6 months). Used once during generation, not persisted beyond that.
- **Trend Indicator**: A derived classification attached to an insight, describing how the correlation strength has changed over the viewed time period. One of "Strengthening", "Weakening", or "Stable". Computed dynamically from sub-period correlation comparisons; not permanently stored.
- **Menstrual Cycle Model**: An internal model used by the generator to produce consistent period bleeding patterns. Defined by cycle length (~28 days ±2), bleeding duration (~4–6 days), and a pre-menstrual window (2–3 days before bleeding) that affects mood and energy.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can generate mock data for either time range with a single interaction, and the data populates within a few seconds.
- **SC-002**: At least 4 of the 5 designed correlations (medication–dizziness, medication–mood, period–mood/energy, exercise–tiredness, tiredness weakening over time) are detectable by the existing correlation engine and appear as insights.
- **SC-003**: Period bleeding entries in the mock data follow a recognisable ~28-day cycle pattern when reviewed on the calendar view.
- **SC-004**: Trend indicators appear on at least one insight in the "Last 3 Months" view and at least one in the "Last Year" view when 6 months of mock data is generated.
- **SC-005**: The tiredness–exercise insight shows a "Weakening" trend indicator in the "Last 3 Months" or "Last Year" view, confirming the designed pattern is detectable.
- **SC-006**: No trend indicators appear in the "Last Month" view regardless of data.
- **SC-007**: 90% of users who see the mock data insights can identify at least one meaningful health pattern without guidance.

## Assumptions

- The existing correlation engine (CorrelationEngine) and insight text generator (InsightTextGenerator) from specs 004 and 006 are already functional. This feature builds on them by providing better input data and adding trend computation on top of existing correlation output.
- The current insight card UI supports displaying additional metadata (trend indicators) without a major redesign — the indicator is a small annotation added to existing cards.
- The menstrual cycle model uses a simplified average (~28 days). Real-world cycle variability is not in scope; the mock data aims for demonstrably recognisable patterns, not clinical accuracy.
- The "several days" threshold for medication–dizziness correlation is defined as 3+ consecutive days without a medication entry. This is a reasonable default for demonstrating the pattern.
- Scale types (Mood, Energy Levels) use the existing 0–5 integer range already supported in the app.
