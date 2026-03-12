<!--
  Sync Impact Report
  Version change: 1.2.0 → 1.3.0
  Modified principles:
    - Added Principle VI. Delight & Motion Design
    - Updated UI & Design Standards with animation requirements
  Added sections: Principle VI. Delight & Motion Design
  Removed sections: None
  Templates requiring updates:
    - .specify/templates/plan-template.md ✅ reviewed (no changes needed)
    - .specify/templates/spec-template.md ✅ reviewed (no changes needed)
    - .specify/templates/tasks-template.md ✅ reviewed (no changes needed)
  Follow-up TODOs: None
-->

# BioGraphApp Constitution

## Core Principles

### I. Material 3 Design Consistency

All UI MUST be built using Material 3 components and theming
exclusively. Custom-drawn UI elements are prohibited unless
Material 3 provides no viable component.

- Every screen MUST use the app's `MyApplicationTheme` wrapper
  as the root composable theme.
- Colors MUST be defined in the Material 3 color scheme
  (`ColorScheme`) in `ui/theme/Color.kt` — never hardcoded
  in individual composables.
- Typography MUST use the centralized `Typography` definition
  in `ui/theme/Type.kt`. Ad-hoc `TextStyle` or inline
  `fontSize`/`fontWeight` overrides are forbidden.
- Spacing and sizing MUST use a consistent 4dp/8dp grid.
  Define reusable dimension constants rather than scattering
  magic numbers.
- Icons MUST come from `androidx.compose.material.icons` or a
  single agreed-upon icon set. Mixing icon families is
  prohibited.

**Rationale**: A unified design language eliminates visual
inconsistency and ensures every screen feels like it belongs
to the same app.

### II. Compose-First UI Architecture

All UI MUST be implemented as Jetpack Compose composables.
No XML layouts, Fragments, or View-based UI are permitted.

- Composables MUST be stateless wherever possible; state MUST
  be hoisted to the caller or a ViewModel.
- Every reusable UI element MUST live in a dedicated file under
  `ui/components/` and accept its data via parameters — never
  by reaching into global state.
- Screen-level composables MUST reside in feature-specific
  packages (e.g., `ui/screens/profile/`) and receive a
  ViewModel or state holder as their single source of truth.
- Side effects MUST use the correct Compose effect handler
  (`LaunchedEffect`, `DisposableEffect`, `SideEffect`) — never
  raw coroutine launches inside composition.
- `@Preview` annotations MUST accompany every public composable
  to enable rapid visual iteration in Android Studio.

**Rationale**: Declarative, stateless composables are easier to
reason about, reuse, and keep visually consistent.

### III. Clean Kotlin Structure

Code MUST follow a clear separation-of-concerns pattern
using MVVM with unidirectional data flow.

- Each feature MUST be organized as:
  `ui/screens/<feature>/` for composables,
  `viewmodel/` for ViewModels, `data/` for repositories and
  data sources.
- ViewModels MUST expose UI state via `StateFlow` or Compose
  `State`. Mutable state MUST NOT leak outside the ViewModel.
- Business logic MUST NOT live inside composables. Composables
  render state; ViewModels transform it.
- Kotlin idioms MUST be preferred: data classes for models,
  sealed classes/interfaces for UI state, extension functions
  for utility logic.
- Package names MUST follow the existing
  `com.martynamaron.biograph.*` namespace hierarchy.

**Rationale**: Consistent architecture makes navigation, code
review, and onboarding straightforward even in a fast-moving
prototype.

### IV. Simplicity & Pragmatism

This project is a prototype. Every decision MUST favour the
simplest working solution.

- YAGNI is the default: do NOT add abstractions, interfaces,
  or layers for hypothetical future needs.
- No dependency injection framework is required — manual
  constructor injection or simple object graphs are sufficient.
- No formal testing infrastructure is required. Code MUST
  still be structured so that logic *could* be tested (pure
  functions, no hidden dependencies), but writing tests is
  not mandated.
- Third-party libraries MUST be added only when they solve
  a concrete, immediate problem. Justify each new dependency
  in the commit message.
- Avoid premature optimization. Correct and readable code
  comes first; optimize only when a measurable problem exists.

**Rationale**: Prototypes deliver value through speed of
iteration. Unnecessary complexity slows iteration without
proportional benefit.

### V. Local Database

All persistent data MUST be stored in an on-device database
with no external server or cloud dependencies.

- Use Room (over raw SQLite) for all local persistence.
  Define entities as `@Entity` data classes and DAOs as
  `@Dao` interfaces in a `data/local/` package.
- Database schema changes MUST use Room auto-migrations.
  Never rely on destructive "drop and recreate" outside of
  initial development.
- Data access MUST go through a repository layer — neither
  composables nor ViewModels may execute queries directly.
- The database MUST be pre-seeded with demo data (via
  `RoomDatabase.Callback` or a pre-packaged `.db` file) so
  the prototype demo works immediately after install.
- Sensitive data (API keys, tokens) MUST NOT be committed
  to version control. Use `local.properties` or environment
  variables.

**Rationale**: An embedded on-device database keeps the
prototype fully self-contained and ensures a smooth demo
with zero setup.

### VI. Delight & Motion Design

The app MUST feel sleek, modern, and delightful to use.
Every interaction should include purposeful motion that
reinforces spatial relationships and draws the user's
attention.

- Screen transitions MUST use animated navigation
  (e.g., `AnimatedNavHost`, shared-element transitions)
  — hard cuts between screens are prohibited.
- Interactive elements (buttons, cards, FABs) MUST include
  tactile feedback: ripple effects, scale/elevation changes
  on press, and smooth state transitions.
- List items MUST animate in on first appearance
  (e.g., staggered fade + slide) rather than popping in
  all at once.
- State changes (loading → content, empty → populated,
  error → retry) MUST use `AnimatedVisibility`,
  `AnimatedContent`, or `Crossfade` — never abrupt swaps.
- Micro-interactions (toggle switches, checkboxes, progress
  indicators) MUST use `animate*AsState` or `Animatable`
  for smooth value transitions.
- Motion MUST follow Material 3 motion guidelines:
  use `tween` with standard easing curves and durations
  between 150ms – 500ms. Avoid gratuitous or slow
  animations that impede usability.
- Shared-element or container-transform transitions SHOULD
  be used when navigating between a summary item and its
  detail screen to reinforce spatial continuity.

**Rationale**: Thoughtful animation transforms a functional
prototype into an experience that feels polished and
professional. Motion provides context, guides attention,
and creates an emotional connection with users.

## UI & Design Standards

- Every screen MUST render correctly in both portrait and
  landscape orientations, or explicitly lock to portrait with
  documented justification.
- Touch targets MUST meet the Material 3 minimum of 48dp.
- Text MUST respect the user's system font-size preference
  (use `sp` units via Compose's default text sizing).
- Loading states MUST be represented with Material 3 progress
  indicators — never blank screens.
- Error states MUST display user-friendly messages using
  Material 3 Snackbar or dialog components.
- Navigation MUST use Jetpack Navigation Compose with
  `AnimatedNavHost` and meaningful enter/exit transitions.
  Deep-linking support is optional for now.
- All visual state changes MUST be animated. Static,
  instantaneous UI updates are considered a defect unless
  the change is imperceptible to the user.
- Elevation and shadow animations SHOULD accompany
  interactive surface changes (e.g., card lift on drag,
  FAB expansion).

## Development Workflow

- Each feature or fix MUST be developed on a dedicated branch
  named `<short-description>` (e.g., `profile-screen`,
  `fix-theme-colors`).
- Commits MUST use conventional commit prefixes:
  `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`.
- Code MUST compile without warnings before being considered
  complete. Suppress warnings only with an inline comment
  explaining why.
- Compose `@Preview` checks are the primary visual validation
  mechanism in lieu of automated UI tests.

## Governance

This constitution is the authoritative guide for all development
decisions in BioGraphApp. When in doubt, refer here.

- Amendments MUST be documented with a version bump, rationale,
  and updated `LAST_AMENDED_DATE`.
- Versioning follows semantic versioning:
  - MAJOR: principle removed or fundamentally redefined.
  - MINOR: new principle or section added.
  - PATCH: wording clarifications or typo fixes.
- All code contributions MUST comply with these principles.
  Non-compliance MUST be flagged and resolved before merge.

**Version**: 1.3.0 | **Ratified**: 2026-03-12 | **Last Amended**: 2026-03-12
