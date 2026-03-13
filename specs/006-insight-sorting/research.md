# Research: Insight Sorting

**Feature**: 006-insight-sorting  
**Date**: 13 March 2026

## 1. Sort Toggle UI Component

**Decision**: Use Material 3 `SingleChoiceSegmentedButtonRow` with `SegmentedButton`.

**Rationale**: This is the canonical M3 component for binary/ternary mode selection. It is available in the Compose Material3 library already included in the project (BOM 2026.03.00). It renders as a horizontal row of outlined buttons with a filled selection indicator — visually distinct from the `PrimaryTabRow` already used for time period tabs, avoiding confusion between the two controls.

**Alternatives considered**:
- `TabRow` — rejected because the insights panel already uses `PrimaryTabRow` for time periods; using another tab row creates visual ambiguity.
- Custom toggle composable — rejected per constitution Principle I (no custom-drawn UI when M3 provides a viable component) and Principle IV (simplicity).
- `FilterChip` pair — rejected because chips are for filter/tag selection, not mutually exclusive mode switching.

## 2. Persisting Sort Mode Preference

**Decision**: Create a `UserPreferenceEntity` (key/value) in Room and store the sort mode as a string value.

**Rationale**: The app already uses Room exclusively for all persistence (constitution Principle V). There is no DataStore or SharedPreferences in the project. A key/value entity is the simplest addition — it reuses the existing DB, requires one new entity + DAO, and one auto-migration. The single-row pattern (like `AnalysisMetadataEntity`) is already established.

**Alternatives considered**:
- Jetpack DataStore — rejected because it adds a new dependency (violates Principle IV: justify each new dependency) and the project's established pattern is Room-only.
- SharedPreferences — rejected for the same reason; also deprecated in favour of DataStore.
- Hardcode in ViewModel without persistence — rejected because the spec requires preference persistence across sessions (FR-008).

## 3. Collapsible Group Animation

**Decision**: Use `AnimatedVisibility` with `expandVertically` / `shrinkVertically` transitions for collapsible data type groups.

**Rationale**: `AnimatedVisibility` is the idiomatic Compose API for show/hide transitions and is already used elsewhere in the app (constitution Principle VI requires smooth state transitions using `AnimatedVisibility`, `AnimatedContent`, or `Crossfade`). The `expandVertically` / `shrinkVertically` animations provide the natural "accordion" motion users expect from collapsible sections.

**Alternatives considered**:
- Custom `Animatable` height animation — rejected as unnecessarily complex (Principle IV).
- `AnimatedContent` — designed for swapping content, not collapsing/expanding in place.

## 4. Strength Tier Colour Tokens

**Decision**: Add three semantic colour values to `Color.kt` and map them in the theme: `strengthStrong`, `strengthModerate`, `strengthMild`. Use existing M3 semantic slots where possible (e.g., `primary` for Strong, `secondary` for Moderate, `outline` for Mild).

**Rationale**: The spec (clarification Q2) requires semantic theme tokens, not hardcoded colours. Rather than adding non-standard colour slots (which would require a custom theme extension), leverage existing M3 `colorScheme` slots that naturally convey different emphasis levels. The StrengthBadge composable will select the correct `colorScheme` value based on the tier.

**Alternatives considered**:
- Custom CompositionLocal for strength colours — rejected as over-engineering for 3 colour values (Principle IV).
- Hardcoded hex values in the composable — rejected per spec clarification (must use semantic tokens).
- CompositionLocal with a data class — provides good extensibility but violates YAGNI for a prototype.

## 5. Grouping Logic Placement

**Decision**: Implement grouping logic in `InsightViewModel` rather than in the composable layer.

**Rationale**: Constitution Principle III — "Business logic MUST NOT live inside composables. Composables render state; ViewModels transform it." Sorting and grouping are data transformations (business logic). The ViewModel will expose two derived state properties: one for the flat strength-sorted list and one for the grouped-by-data-type map. The composable simply renders whichever is active.

**Alternatives considered**:
- Grouping in the composable via `remember` + `derivedStateOf` — rejected because this puts transformation logic in the UI layer.
- Separate use-case/interactor class — rejected as over-engineering for a prototype (Principle IV).

## 6. "Also in" Cross-Reference Tag

**Decision**: Compute the cross-reference text in the ViewModel when building the grouped state. Each insight in a group carries a nullable `alsoInDataTypeName: String?` field (non-null when the insight appears in another group).

**Rationale**: The ViewModel already has access to all data type names (via `DataTypeRepository`). Computing the cross-reference tag during grouping avoids repeated lookups in the composable layer. A simple data class wrapper (`GroupedInsight`) pairs the `InsightEntity` with the optional tag.

**Alternatives considered**:
- Compute in the composable by looking up data type names — rejected per Principle III (no logic in composables).
- Add the tag to `InsightEntity` in Room — rejected because this is a presentation-only concern, not persisted data.

## 7. Database Migration Strategy

**Decision**: Room auto-migration from version 3 → 4. The only schema change is adding the `user_preferences` table.

**Rationale**: Auto-migration handles simple additive schema changes (new tables, new columns). The `UserPreferenceEntity` table is additive — no existing tables are modified. This matches the established pattern (v2→v3 auto-migration already exists in `AppDatabase`).

**Alternatives considered**:
- Manual Migration — unnecessary overhead for a simple table addition.
- Destructive migration — prohibited by constitution Principle V ("Never rely on destructive drop and recreate outside of initial development").
