# Quickstart: Brand Theme Refresh

**Feature**: 005-brand-theme-refresh  
**Date**: 2026-03-13

## Prerequisites

- Android Studio with Kotlin and Compose support
- BioGraphApp project cloned and building successfully
- On branch `005-brand-theme-refresh`

## Implementation Order

### Step 1: Update Color.kt

Replace all purple/pink colour definitions with the green brand palette.

**File**: `app/src/main/java/com/martynamaron/biograph/ui/theme/Color.kt`

**Changes**:
- Remove: `Purple80`, `PurpleGrey80`, `Pink80`, `Purple40`, `PurpleGrey40`, `Pink40`
- Add: `GreenDarkest` (#15603E), `GreenMid` (#1E8D5A), `GreenLightest` (#6CC19F), `GrayBackground` (#D4D2D2)
- Add derived tones: `SecondaryContainer` (#B8E6D0), `TertiaryContainer` (#E0F5EC)

### Step 2: Update Theme.kt

Replace the theme function to use brand colours and disable dynamic colour.

**File**: `app/src/main/java/com/martynamaron/biograph/ui/theme/Theme.kt`

**Changes**:
- Define `LightColorScheme` using the full Material 3 role mapping from [research.md](research.md) (Research Task 1)
- Remove `DarkColorScheme` (out of scope)
- Remove dynamic colour logic (`dynamicColor` parameter, `Build.VERSION` check, `dynamicLightColorScheme`/`dynamicDarkColorScheme`)
- Simplify `MyApplicationTheme` to always apply `LightColorScheme`

### Step 3: Add Logo to OnboardingScreen

**File**: `app/src/main/java/com/martynamaron/biograph/ui/screens/onboarding/OnboardingScreen.kt`

**Changes**:
- Add `Image(painterResource(R.drawable.black_logo), ...)` before the "Welcome to BioGraph" Text
- Size: ~120dp × 150dp with `ContentScale.Fit`
- Wrap in `AnimatedVisibility` with fade-in (per constitution Principle VI)
- Remove or reduce the existing top `Spacer(48.dp)` to accommodate the logo

### Step 4: Add Icon + Wordmark to Calendar TopAppBar

**File**: `app/src/main/java/com/martynamaron/biograph/ui/screens/calendar/CalendarScreen.kt`

**Changes**:
- Replace `title = { Text("BioGraph") }` with a `Row` containing:
  - `Image(painterResource(R.drawable.black_logo), modifier = Modifier.size(24.dp, 30.dp))`
  - `Spacer(Modifier.width(8.dp))`
  - `Text("BioGraph", style = MaterialTheme.typography.titleLarge)`

### Step 5: Clean Up colours.xml

**File**: `app/src/main/res/values/colors.xml`

**Changes**:
- Remove unused: `purple_200`, `purple_500`, `purple_700`, `teal_200`, `teal_700`
- Keep: `green_darkest`, `green_mid`, `green_lightest`, `gray_background`, `black`, `white`

### Step 6: Update Launcher Icon

**Files**:
- `app/src/main/res/drawable/ic_launcher_foreground.xml` — Replace Android robot with leaf logo, scaled for 108dp adaptive icon viewport
- `app/src/main/res/drawable/ic_launcher_background.xml` — Replace `#3DDC84` green grid with solid `#D4D2D2` grey fill

### Step 7: Add @Preview Annotations

Per constitution, every modified composable must have `@Preview` annotations. Add previews for:
- OnboardingScreen (showing logo placement)
- CalendarScreen TopAppBar (showing icon-wordmark)

## Verification

1. Build and run the app
2. Navigate all 4 screens — no purple/pink visible anywhere
3. Check Onboarding: logo is top-center above "Welcome to BioGraph"
4. Check Calendar: leaf icon + "BioGraph" text in top app bar
5. Check home screen: launcher icon shows leaf on grey background
6. Visual check: text on all coloured surfaces is readable
