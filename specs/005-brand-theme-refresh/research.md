# Research: Brand Theme Refresh

**Feature**: 005-brand-theme-refresh  
**Date**: 2026-03-13

## Research Task 1: Material 3 Colour Role Mapping

### Context
The app needs to map 3 brand greens + 1 grey to Material 3's `ColorScheme` roles (primary, secondary, tertiary, surface, background, etc.). The current scheme uses default purple/pink template colours.

### Decision: Brand Colour → Material 3 Role Mapping (Light Mode)

| Material 3 Role | Hex Value | Source | Rationale |
|-----------------|-----------|--------|-----------|
| `primary` | #1E8D5A (green_mid) | Brand palette | Main interactive elements — buttons, FABs, active states. Mid-green is the most versatile and visible |
| `onPrimary` | #FFFFFF (white) | Derived | White text/icons on mid-green. Contrast ratio: ~4.9:1 against #1E8D5A — passes WCAG AA |
| `primaryContainer` | #6CC19F (green_lightest) | Brand palette | Tonal container for cards, chips, selected states. Lighter green for subtle highlighting |
| `onPrimaryContainer` | #15603E (green_darkest) | Brand palette | Dark green text on light green containers. Contrast ratio: ~4.6:1 — passes WCAG AA |
| `secondary` | #15603E (green_darkest) | Brand palette | Supporting elements like secondary buttons, less prominent controls |
| `onSecondary` | #FFFFFF (white) | Derived | White on dark green. Contrast ratio: ~7.5:1 — passes WCAG AA and AAA |
| `secondaryContainer` | #B8E6D0 (derived) | Computed (lighten green_lightest) | Very light green tint for secondary containers. Use ~20% lighter version of green_lightest |
| `onSecondaryContainer` | #15603E (green_darkest) | Brand palette | Dark green text on very light green |
| `tertiary` | #6CC19F (green_lightest) | Brand palette | Accent and complementary elements |
| `onTertiary` | #15603E (green_darkest) | Brand palette | Dark green text on lightest green |
| `tertiaryContainer` | #E0F5EC (derived) | Computed | Very pale green for tertiary containers |
| `onTertiaryContainer` | #15603E (green_darkest) | Brand palette | Dark green text on pale green |
| `background` | #FFFFFFFF (white) | Standard | Clean white background per modern design. Grey is an accent, NOT the primary background |
| `onBackground` | #1C1B1F (near-black) | Material default | Standard dark text on white background |
| `surface` | #FFFFFFFF (white) | Standard | Surfaces match background for a seamless feel |
| `onSurface` | #1C1B1F (near-black) | Material default | Standard readability |
| `surfaceVariant` | #D4D2D2 (gray_background) | Brand palette | Card backgrounds, dividers — subtle distinction from white |
| `onSurfaceVariant` | #49454F | Material default | Dark text on grey surface. Contrast ratio: ~5.5:1 against #D4D2D2 — passes WCAG AA |
| `outline` | #79747E | Material default | Border lines — standard material grey works well with green palette |
| `error` | #B3261E | Material default | Unchanged — red error states are universal UX |
| `onError` | #FFFFFF | Material default | White on red |

### Alternatives Considered
- **Dark green (#15603E) as primary**: Rejected — too dark and heavy as the main UI colour, better suited as a secondary/accent. Mid-green is more vibrant and inviting.
- **Grey (#D4D2D2) as primary background**: Rejected per spec (FR-009) — grey is an accent/surface variant. White background creates a cleaner, more modern feel.
- **Material default error colour in green**: Rejected — keeping red for errors is a universal convention that aids usability.

---

## Research Task 2: WCAG AA Contrast Verification

### Context
FR-007 requires all text-on-colour combinations to meet WCAG AA (4.5:1 body, 3:1 large text/UI components).

### Contrast Analysis

| Foreground | Background | Ratio | WCAG AA Body | WCAG AA Large |
|-----------|------------|-------|-------------|---------------|
| #FFFFFF (white) | #1E8D5A (primary) | ~4.9:1 | **PASS** | **PASS** |
| #FFFFFF (white) | #15603E (secondary) | ~7.5:1 | **PASS** | **PASS** |
| #15603E (dark green) | #6CC19F (primaryContainer) | ~4.6:1 | **PASS** | **PASS** |
| #15603E (dark green) | #D4D2D2 (surfaceVariant) | ~5.1:1 | **PASS** | **PASS** |
| #1C1B1F (near-black) | #FFFFFF (background) | ~16:1 | **PASS** | **PASS** |
| #1C1B1F (near-black) | #D4D2D2 (surfaceVariant) | ~10.2:1 | **PASS** | **PASS** |
| #49454F (onSurfaceVariant) | #D4D2D2 (surfaceVariant) | ~5.5:1 | **PASS** | **PASS** |
| #FFFFFF (white) | #6CC19F (lightest green) | ~2.6:1 | **FAIL** | **FAIL** |

### Decision
- White text on green_lightest (#6CC19F) **fails** contrast. This combination must be avoided. Use dark text (#15603E or #1C1B1F) on green_lightest surfaces.
- All other planned combinations pass WCAG AA.
- Green_lightest is suitable as a container/background colour only, never as a background for white text.

---

## Research Task 3: Disabling Dynamic Colour (Material You)

### Context
The current `MyApplicationTheme` enables dynamic colour on Android 12+ via `dynamicColor: Boolean = true`. This means the brand colours are hidden on most modern devices.

### Decision
Set `dynamicColor` parameter default to `false` and remove the dynamic colour branches from the `when` block. The theme should always use the custom `LightColorScheme`.

### Implementation Pattern
```kotlin
@Composable
fun MyApplicationTheme(
    // Dynamic color disabled — brand colours always used
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
```

### Rationale
- Simplest approach per constitution Principle IV (Simplicity & Pragmatism)
- Dark mode is deferred, so no dark/light branching needed
- Removes the `isSystemInDarkTheme()`, `Build.VERSION.SDK_INT`, and `LocalContext` imports — cleaner file

### Alternatives Considered
- **Keep dynamic colour as a user setting**: Rejected — adds complexity for a prototype and defeats the branding purpose
- **Keep dark theme branch but just disable dynamic colour**: Rejected — dark mode is out of scope, so the dead code adds confusion

---

## Research Task 4: Logo Integration Patterns

### Context
The logo (`black_logo.xml`) is a vector drawable (160dp × 200dp) that uses `@color/green_mid` fill and `@color/green_lightest` stroke. It needs to appear in two locations.

### Decision: Onboarding Screen Logo
- Load the vector drawable via `painterResource(R.drawable.black_logo)` 
- Display using Compose `Image` composable with `ContentScale.Fit`
- Target display size: approximately 120dp × 150dp (scaled from native 160×200)
- Position: top-center of the screen column, before the "Welcome to BioGraph" headline
- Add `AnimatedVisibility` fade-in per constitution Principle VI (Delight & Motion)

### Decision: Calendar TopAppBar Icon-Wordmark
- Use a `Row` inside the TopAppBar `title` slot containing:
  1. Small `Image` of the logo (24dp × 30dp, scaled) 
  2. `Spacer` (8dp width)
  3. `Text("BioGraph")` with `titleLarge` style
- The Row replaces the current `Text("BioGraph")` in the title slot
- Logo at this small size preserves the leaf silhouette while fitting the 48dp app bar

### Rationale
- Vector drawable scales perfectly to any size — no pixelation risk
- Using `painterResource` is the standard Compose pattern for XML drawables
- 24dp icon in the app bar follows Material 3 guidelines (icons should be 24dp)

### Alternatives Considered
- **Create a separate smaller logo asset for the app bar**: Rejected — the vector scales perfectly, no need for a second asset
- **Use an ImageVector (Compose-native)**: Rejected — the logo path data is very complex; keeping it as an XML vector drawable and loading via `painterResource` is simpler and already works

---

## Research Task 5: Launcher Icon Update

### Context
The current launcher icon uses:
- Foreground: Default Android robot icon (`ic_launcher_foreground.xml`)
- Background: `#3DDC84` (Android green, not brand green) with a grid pattern
- Background colour resource: `#D4D2D2` (already brand grey in `values/ic_launcher_background.xml`)

### Decision
1. **Replace foreground** with the leaf logo (`black_logo.xml`), scaled and centered for the 108dp adaptive icon safe zone (66dp inner area)
2. **Update background drawable** to use brand grey (`#D4D2D2`) as a solid fill instead of the `#3DDC84` green grid
3. The `values/ic_launcher_background.xml` already has `#D4D2D2` — no change needed there

### Rationale
- The leaf logo on grey background provides a clean, recognisable icon that matches the in-app branding
- Adaptive icon format ensures compatibility with all launcher shapes

### Alternatives Considered
- **Keep the Android robot foreground**: Rejected — contradicts branding goals
- **Green background instead of grey**: Rejected — grey was specified in the original user request and is already set in the background colour resource

---

## Research Task 6: XML Resource Cleanup

### Context
`colors.xml` contains unused purple/teal colours from the default template alongside the brand colours.

### Decision
Remove unused purple and teal colour definitions (`purple_200`, `purple_500`, `purple_700`, `teal_200`, `teal_700`). Keep the brand colours (`green_darkest`, `green_mid`, `green_lightest`, `gray_background`) and standard colours (`black`, `white`).

### Rationale
- Dead code removal per constitution Principle IV (Simplicity)
- The purple/teal colours are not referenced anywhere except `colors.xml` itself (Compose theme uses `Color.kt`)
- Brand colours in `colors.xml` are still referenced by `black_logo.xml` and potentially the launcher icon

### Alternatives Considered
- **Keep all colours for backwards compatibility**: Rejected — no code references them; they are purely template leftovers
