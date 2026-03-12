# Research: Add and Track Data

**Date**: 2026-03-12 | **Feature Branch**: `001-add-track-data`

## 1. Room Database Setup with Compose

**Decision**: Use KSP (not KAPT) with Room 2.6.1 for annotation processing and Kotlin code generation.

**Rationale**: KSP is the current standard for Kotlin annotation processing; KAPT is deprecated for new projects. Room 2.6+ generates Kotlin code by default with KSP, providing better performance. DAO methods return `Flow<T>` which Compose collects via `collectAsState()` — Room observes table invalidation and re-emits automatically.

**Alternatives considered**:
- KAPT: Deprecated, slower compile times, generates Java code.
- LiveData return types: Deprecated for Compose; Flow is the idiomatic choice.

**Dependencies to add**:
```kotlin
// libs.versions.toml
room = "2.6.1"
ksp = "2.0.21-1.0.27"  # Must match Kotlin version

// app/build.gradle.kts
ksp(libs.room.compiler)
implementation(libs.room.runtime)
implementation(libs.room.ktx)
```

## 2. Emoji Handling

**Decision**: Use the system emoji keyboard via a standard Compose `TextField` for emoji input. No additional emoji library needed for the prototype.

**Rationale**: The simplest approach — users tap an emoji field, the system keyboard opens with emoji tab. This avoids adding dependencies and works on all devices. For unsupported emoji fallback, use a simple check: if the emoji string renders as a single grapheme cluster, accept it; otherwise show a generic placeholder icon.

**Alternatives considered**:
- AndroidX Emoji2 + EmojiPicker: Adds 2 dependencies for a feature the system keyboard already handles. Could be added later if cross-device rendering issues arise.
- Custom curated grid: High maintenance burden, loses access to full Unicode emoji set. Unnecessary for prototype.

**Implementation notes**:
- Single-line `TextField` restricted to emoji input via keyboard type.
- Validate that input is a single emoji character (use `Character.isEmoji()` on API 35+ or regex pattern for lower APIs).
- Fallback: display a filled circle (⬤) if emoji doesn't render.

## 3. Calendar UI

**Decision**: Build a custom calendar grid using Compose `LazyVerticalGrid` rather than adopting a third-party library.

**Rationale**: For a prototype with a single month view, the calendar grid is straightforward — a 7-column grid with day cells. This avoids adding a dependency (Kizitonwose Calendar) and keeps the project simple per Constitution Principle IV (Simplicity & Pragmatism). The custom implementation is ~100 lines and gives full control over styling, animations, and Material 3 integration.

**Alternatives considered**:
- Kizitonwose Calendar library: Feature-rich but adds a dependency for a relatively simple UI. Better suited if week/year views or complex scrolling were needed.
- Material DatePicker: Only supports single-date selection, not a persistent calendar grid.

**Implementation notes**:
- Use `java.time.LocalDate` and `java.time.YearMonth` (requires core library desugaring for minSdk 24).
- Add `coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")` for java.time support on API < 26.
- Month navigation via previous/next buttons + `AnimatedContent` for slide transitions.
- Each day cell composable shows date number + visual indicator dot for logged days.

## 4. Navigation Compose with Animations

**Decision**: Use core `NavHost` (Navigation 2.8+) with built-in enter/exit transitions. No Accompanist needed.

**Rationale**: Since Navigation 2.8.0, animation support is built into the core `NavHost` composable — the separate Accompanist Navigation Animation library is deprecated. This provides slide + fade transitions out of the box, consistent with Material 3 motion guidelines.

**Alternatives considered**:
- Accompanist Navigation Animation: Deprecated in favor of core NavHost.
- Manual `AnimatedContent`: More verbose; NavHost abstracts transition management.

**Implementation notes**:
- Use type-safe routes with `@Serializable` data classes (Navigation 2.8+ pattern).
- Default enter/exit: `slideInHorizontally() + fadeIn()` / `slideOutHorizontally() + fadeOut()`.
- Duration: 300ms with `EaseInOut` easing per Material 3 motion spec.
- Add Navigation Compose dependency to version catalog.

**Dependencies to add**:
```kotlin
// libs.versions.toml
navigationCompose = "2.8.4"

// app/build.gradle.kts
implementation(libs.navigation.compose)
```

## 5. Room Migrations Strategy

**Decision**: Start with `exportSchema = true` and use destructive migration fallback during initial development. Switch to auto-migrations before any user-facing release.

**Rationale**: During rapid prototyping, schema changes are frequent and there's no user data to preserve. Destructive fallback (`fallbackToDestructiveMigration()`) keeps iteration fast. Before any release, enable `@AutoMigration` annotations and commit schema JSON files to version control.

**Alternatives considered**:
- Auto-migrations from day one: Adds overhead during early prototyping when schema is unstable.
- Manual migrations only: Excessive for a prototype that may never see production.

**Implementation notes**:
- Set `exportSchema = true` in `@Database` annotation from the start (generates schema JSON for future migrations).
- Configure Room Gradle plugin to export schemas to `schemas/` directory.
- Schema JSON files committed to git for migration test support.

## 6. Bottom Sheet for Day Panel

**Decision**: Use Material 3 `ModalBottomSheet` with `rememberModalBottomSheetState()`.

**Rationale**: Native Material 3 component with built-in animations, back-gesture support, and scrim. Matches the spec's "panel or bottom sheet" requirement for the day data toggle view. State management is straightforward with `SheetState` + coroutine scope.

**Alternatives considered**:
- Full-screen dialog: Less discoverable, breaks spatial relationship with calendar.
- Inline expandable panel: More complex layout, risk of pushing calendar content.
- Accompanist BottomSheet: Deprecated in favor of Material 3 native component.

**Implementation notes**:
- `ModalBottomSheet` wraps a column of `DataTypeToggleItem` composables + Save button.
- `SheetState.hide()` is a suspend function — use `rememberCoroutineScope()`.
- Bottom sheet dismisses on back press and scrim tap automatically.

## 7. ViewModel + Compose Integration

**Decision**: Use `lifecycle-viewmodel-compose` for `viewModel()` function in composables. State exposed via `StateFlow` collected with `collectAsStateWithLifecycle()`.

**Rationale**: `collectAsStateWithLifecycle()` (from `lifecycle-runtime-compose`) is lifecycle-aware and stops collection when the UI is not visible, saving resources. This is the recommended pattern for Compose + ViewModel integration.

**Dependencies to add**:
```kotlin
// libs.versions.toml
lifecycleRuntimeCompose = "2.6.1"
lifecycleViewModelCompose = "2.6.1"

// app/build.gradle.kts
implementation(libs.lifecycle.runtime.compose)
implementation(libs.lifecycle.viewmodel.compose)
```

## 8. Core Library Desugaring for java.time

**Decision**: Enable core library desugaring to use `java.time` APIs on minSdk 24 (API < 26).

**Rationale**: `java.time.LocalDate`, `YearMonth`, and related classes are essential for calendar logic but require API 26+. Core library desugaring backports these to minSdk 21+.

**Dependencies to add**:
```kotlin
// app/build.gradle.kts
android {
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}
dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
}
```

## Summary of New Dependencies

| Dependency | Purpose | Version |
|-----------|---------|---------|
| Room runtime | Local database | 2.6.1 |
| Room KSP compiler | Annotation processing | 2.6.1 |
| Room KTX | Kotlin extensions + Flow support | 2.6.1 |
| Navigation Compose | Screen navigation + animations | 2.8.4 |
| Lifecycle ViewModel Compose | `viewModel()` in composables | 2.6.1 |
| Lifecycle Runtime Compose | `collectAsStateWithLifecycle()` | 2.6.1 |
| Desugar JDK Libs | java.time on minSdk 24 | 2.0.3 |
| KSP Gradle Plugin | Kotlin Symbol Processing | 2.0.21-1.0.27 |
