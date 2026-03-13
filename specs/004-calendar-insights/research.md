# Research: Calendar Insights Panel

**Date**: 2026-03-13 | **Feature Branch**: `004-calendar-insights`

## 1. Statistical Correlation Methods for Mixed Data Types

**Decision**: Implement a lightweight, zero-dependency correlation engine in pure Kotlin that computes pairwise correlations between all data type combinations using the appropriate statistical measure for each pair type.

**Rationale**: The app tracks three input types (Toggle, Scale, Multiple Choice), each requiring a different correlation method. All formulas are simple enough to implement in plain Kotlin without any statistics library — aligning with the constitution's Simplicity & Pragmatism principle. The engine runs in O(n × m²) where n = number of days and m = number of data types — fast enough for years of data on any device.

**Alternatives considered**:
- Apache Commons Math / Kotlin Statistics library — rejected as an unnecessary dependency for 3 formulas.
- Generic ML-based pattern detection — rejected as overkill; statistical co-occurrence is sufficient for the described insight format.

### Method Matrix

| Comparison | Method | Range | Threshold for "relevant" (≥60% spec) |
|---|---|---|---|
| TOGGLE ↔ TOGGLE | Phi coefficient (φ) | -1 to +1 | \|φ\| ≥ 0.35 (maps to ~60% co-occurrence) |
| TOGGLE → SCALE | Point-biserial correlation | -1 to +1 | \|r_pb\| ≥ 0.35 |
| SCALE ↔ SCALE | Pearson correlation | -1 to +1 | \|r\| ≥ 0.35 |
| TOGGLE/SCALE → MC option | Mean comparison per option | N/A | Mean difference ≥ 1.0 on 0–5 scale, or co-occurrence ≥ 60% |

### Phi Coefficient (TOGGLE ↔ TOGGLE)

Builds a 2×2 contingency table from day-level presence/absence of two toggle data types, then computes:

$$\phi = \frac{ad - bc}{\sqrt{(a+b)(c+d)(a+c)(b+d)}}$$

Where a = both present, b = first only, c = second only, d = neither.

**Interpretation**: φ > 0 = positive co-occurrence (happen together); φ < 0 = mutually exclusive.

### Point-Biserial Correlation (TOGGLE → SCALE)

Compares mean scale values on days when the toggle is present vs absent:

$$r_{pb} = \frac{M_1 - M_0}{s_n} \sqrt{\frac{n_1 n_0}{n^2}}$$

Where M₁ = mean scale when toggle present, M₀ = mean when absent, sₙ = overall std dev.

### Pearson Correlation (SCALE ↔ SCALE)

Standard linear correlation between two numeric scale values on matching days:

$$r = \frac{\sum(x_i - \bar{x})(y_i - \bar{y})}{\sqrt{\sum(x_i - \bar{x})^2 \sum(y_i - \bar{y})^2}}$$

### Multiple Choice Option Analysis

Each MC option is treated as a binary toggle (selected / not selected on a given day). The engine then applies Phi coefficient (vs toggles) or Point-biserial (vs scales) for each individual option. This explodes a single MC data type into N binary signals (one per option).

### Insight Text Generation (Templated)

**Decision**: Use templated sentence patterns with emoji interpolation as the baseline. Optional on-device LLM enhancement deferred to a follow-up.

**Rationale**: Template-based sentences are deterministic, fast, and require no additional dependencies. The spec's example insights ("You had headaches when you didn't leave the house 80% of the time") are naturally templated. An on-device LLM (Gemini Nano) can enhance phrasing later but is not required for MVP.

**Template patterns**:
- TOGGLE ↔ TOGGLE positive: "{emoji1} {desc1} occurred on {pct}% of days you also had {emoji2} {desc2}"
- TOGGLE ↔ TOGGLE negative: "You rarely had {emoji1} {desc1} on days with {emoji2} {desc2}"
- TOGGLE → SCALE positive: "Your {emoji2} {desc2} was higher on days with {emoji1} {desc1} (avg {mean1} vs {mean0})"
- TOGGLE → SCALE negative: "Your {emoji2} {desc2} tended to be lower on days with {emoji1} {desc1}"
- SCALE ↔ SCALE positive: "{emoji1} {desc1} and {emoji2} {desc2} tended to move together"
- MC option → SCALE: "On days you chose {optEmoji} {optLabel}, your {emoji2} {desc2} averaged {mean}"

**Alternatives considered**: On-device Gemini Nano for natural-language phrasing — deferred as optional enhancement (FR-017). The statistical engine + templates fully satisfy FR-004, FR-005, FR-016.

---

## 2. On-Device LLM (Optional Enhancement — FR-017)

**Decision**: Defer Gemini Nano integration to a follow-up task. The statistical engine with templates is the required baseline. Document the integration path for future use.

**Rationale**:
- Gemini Nano requires Android 14+, specific Tensor hardware (Pixel 8 Pro+), and ~2GB model download — limiting reach.
- The app's minSdk is 24 — Gemini Nano would only work on a fraction of devices.
- Template-based insights fully satisfy the spec requirements.
- Constitution Principle IV (Simplicity) favours shipping the working baseline first.

**Future integration path**: Google's `com.google.ai.client.generativeai` SDK with `modelName = "gemini-nano"`. The `InsightRepository` would gain an optional `InsightPhraser` that, when Gemini Nano is available, rephrases templated insights into more natural language. Fallback to templates when unavailable.

**Alternatives considered**: Google ML Kit text classification — rejected because it classifies text, doesn't generate it. TensorFlow Lite with a custom model — rejected as too much effort for phrasing polish.

---

## 3. Room Database Pattern for Persisted Insights

**Decision**: Add an `InsightEntity` table and an `AnalysisMetadataEntity` (single-row) table to the existing `AppDatabase`. DB version 2 → 3 via Room auto-migration.

**Rationale**:
- Same-database storage is simpler (single connection, single backup, atomic transactions).
- `InsightEntity` stores computed insights with foreign keys to the data types involved. CASCADE DELETE ensures insights are cleaned up when a data type is deleted (edge case from spec).
- `AnalysisMetadataEntity` is a single-row table (`id = 0`) that tracks the last analysis timestamp and data count — enabling the "re-analyse only when data changed" requirement (FR-011).
- Room auto-migration handles additive table creation identically to the v1→v2 migration in spec 003.

**Alternatives considered**:
- Separate SQLite database for insights — rejected as unnecessary complexity for a prototype.
- SharedPreferences for metadata — rejected to keep all structured data in Room for consistency.
- In-memory cache only — rejected because spec FR-010 requires persistence across app restarts.

### Re-Analysis Detection Logic

The `AnalysisMetadataEntity` stores `lastDataCount` (total daily entries + multi-choice selections at time of last analysis). On Calendar screen open, the ViewModel compares current count vs stored count. If different → re-analyse. If same → load persisted insights.

This is simpler and more reliable than timestamp-based comparison, and avoids issues with clock skew.

---

## 4. Compose Animation for Custom Delightful Loader

**Decision**: Implement a custom animated loader using Compose's `rememberInfiniteTransition` API — a set of pulsing, shimmering dots with staggered timing. Wrap the loading → content transition in `AnimatedContent` with a smooth crossfade.

**Rationale**:
- Constitution Principle VI mandates custom motion design. Default `CircularProgressIndicator` is explicitly excluded by the spec (FR-002).
- `InfiniteTransition` is the idiomatic Compose API for continuous animations. No external animation library needed.
- Staggered dot animation (3 dots bouncing with offset timing) is lightweight, visually pleasant, and matches the "crafted, smooth" requirement.
- `AnimatedContent` with `fadeIn + fadeOut` handles the loader → insights transition per Principle VI's mandate for no "abrupt swaps".

**Animation specs**:
- Dot bounce: `tween(600ms, EaseInOutCubic)` with 100ms stagger per dot
- Crossfade loader → content: `tween(300ms, EaseInOutCubic)`
- Subtext below dots: "Discovering patterns..." in `bodySmall` with animated alpha

**Alternatives considered**:
- Lottie animation — rejected as a new dependency (Principle IV).
- Shimmer skeleton loading — considered but less "delightful" than animated dots for a small panel.
- Canvas-drawn custom animation — rejected as "custom-drawn UI" (Principle I).

---

## 5. Calendar Screen Layout Integration

**Decision**: Insert the insights panel as a new composable below the `AnimatedContent` calendar grid, inside the existing `Column` in `CalendarScreen`. Wrap the full Column content in a `verticalScroll` modifier to ensure the calendar grid + insights panel can scroll together when content exceeds the screen.

**Rationale**:
- The current `CalendarScreen` has a `Column` with the calendar grid that fills available space. Adding an insights panel below would overflow on smaller screens without scrolling.
- Using `verticalScroll` on the Column (rather than converting to `LazyColumn`) is simpler and avoids nesting scroll containers with the existing `LazyVerticalGrid` inside `AnimatedContent`.
- The insights panel sits at the natural "bottom of screen" position per the spec requirement.

**Alternatives considered**:
- Bottom sheet for insights — rejected because the DayPanel already uses `ModalBottomSheet` and a second sheet would conflict.
- Separate tab/screen for insights — rejected because the spec explicitly says "bottom part of the Calendar screen".
- Fixed-height non-scrollable panel — rejected because insight count varies and the calendar grid should remain accessible.
