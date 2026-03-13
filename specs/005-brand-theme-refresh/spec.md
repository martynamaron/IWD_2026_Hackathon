# Feature Specification: Brand Theme Refresh

**Feature Branch**: `005-brand-theme-refresh`  
**Created**: 2026-03-13  
**Status**: Draft  
**Input**: User description: "I added new green colours and one grey for background. I also added a logo for the app. I'd like to change the colour theme of the app to match the new colours. I also want to include the logo in the interface somewhere. Focus on good branding, sleek designs and modern feel."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cohesive Green Brand Theme (Priority: P1)

As a user, when I open BioGraph I see a consistent green colour palette used throughout the entire app — buttons, top bars, accent elements, and interactive controls all reflect the brand's green tones instead of the current default purple/pink Material theme. The overall look feels modern, clean, and nature-inspired, aligning with the bio/health tracking purpose of the app.

**Why this priority**: The colour theme is the single most impactful visual change. It transforms the entire look and feel of the app across every screen in one go, replacing the default Android template colours with a purposeful brand identity.

**Independent Test**: Can be fully tested by launching the app and navigating through all four screens (Onboarding, Calendar, Data Type List, Settings) and verifying that all interactive elements, surfaces, and accents use the green colour palette rather than purple/pink defaults.

**Acceptance Scenarios**:

1. **Given** the app is launched, **When** the user views any screen, **Then** primary interactive elements (buttons, FABs, toggles, selected states) use the brand green palette
2. **Given** the app is launched, **When** the user views any screen, **Then** top app bars use branding-consistent colours (green-toned or neutral) instead of default purple
3. **Given** the app is in light mode, **When** viewing background surfaces, **Then** backgrounds use the brand grey (#D4D2D2) or white, providing clean contrast with green accents
4. **Given** the user taps an interactive element (button, chip, toggle), **When** it changes state, **Then** the active/selected state uses a distinguishable shade from the green palette

---

### User Story 2 - Logo on the Onboarding Screen (Priority: P2)

As a new user opening BioGraph for the first time, I see the app's leaf logo prominently displayed on the Onboarding screen, giving me an immediate sense of the app's brand identity and purpose. The logo feels intentionally placed, well-sized, and visually polished.

**Why this priority**: The Onboarding screen is the first impression for every new user. Placing the logo here establishes brand recognition from the very first interaction without cluttering day-to-day screens.

**Independent Test**: Can be fully tested by clearing app data and opening the app — the Onboarding screen should display the logo in a prominent, visually balanced position.

**Acceptance Scenarios**:

1. **Given** the user opens the app for the first time, **When** the Onboarding screen is displayed, **Then** the BioGraph leaf logo is visible at the top-center of the screen, above the onboarding headline and setup controls
2. **Given** the Onboarding screen is displayed, **When** the user views the logo, **Then** it appears crisp at the displayed size with no pixelation or clipping
3. **Given** different device sizes (phone and tablet), **When** the Onboarding screen loads, **Then** the logo scales appropriately and maintains visual balance within the layout

---

### User Story 3 - Logo in the Top App Bar (Priority: P3)

As a returning user, I see a compact version of the BioGraph logo or wordmark in the top app bar of the main Calendar screen. This reinforces the brand identity during regular use without being intrusive or taking up excessive space.

**Why this priority**: The Calendar screen is the most-used screen in the app. A subtle logo presence here strengthens brand recall during daily use while keeping the interface clean and functional.

**Independent Test**: Can be fully tested by navigating to the Calendar screen and verifying the logo/brand mark is visible in the top app bar alongside existing navigation elements.

**Acceptance Scenarios**:

1. **Given** the user is on the Calendar screen, **When** they look at the top app bar, **Then** a small leaf logo icon followed by "BioGraph" text is visible in the title area
2. **Given** the top app bar displays the logo, **When** compared to other action icons (Settings gear), **Then** the logo does not crowd or push other elements off-screen
3. **Given** various screen widths, **When** the Calendar screen is displayed, **Then** the logo/brand element adapts gracefully (no truncation or overlap)

---

### User Story 4 - Updated App Launcher Icon (Priority: P4)

As a user looking at my home screen or app drawer, I see the BioGraph app icon using the brand green colour scheme with a clean, modern look that matches the in-app experience.

**Why this priority**: The launcher icon is the entry point to the app and should match the refreshed brand, but it has less immediate impact than the in-app experience.

**Independent Test**: Can be fully tested by installing the app and checking the launcher icon on the home screen and app drawer against the brand palette.

**Acceptance Scenarios**:

1. **Given** the app is installed, **When** the user views the home screen or app drawer, **Then** the launcher icon uses the brand green palette with a grey or white background
2. **Given** different Android launcher styles (round, squircle, square), **When** displaying the icon, **Then** it renders correctly with no important elements cropped

---

### Edge Cases

- What happens when the device uses high-contrast or accessibility colour settings? The green theme must still meet WCAG AA contrast ratios (4.5:1 for body text, 3:1 for large text/UI components)
- How does the theme appear on devices running Android 12+ where dynamic colour (Material You) can override app themes? The app should use its own brand colours and disable dynamic colour theming
- What happens on very small screens (< 320dp width)? Logo and brand elements must not overflow or obscure functional UI elements
- How does the brand grey background (#D4D2D2) look against white text? Text on grey backgrounds must pass contrast requirements — dark text should be used on the grey background

## Clarifications

### Session 2026-03-13

- Q: Where should the logo be placed on the Onboarding screen? → A: Top-center of the screen, above the onboarding content (headline + setup controls sit below)
- Q: Should the Calendar top bar show a logo icon, text wordmark, or both? → A: Small leaf logo icon + "BioGraph" text next to it (icon-wordmark combo)
- Q: Is dark mode in scope for this feature? → A: Light mode only; dark mode deferred to a future iteration

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST replace all default purple/pink theme colours with the brand green palette (darkest: #15603E, mid: #1E8D5A, lightest: #6CC19F) and brand grey (#D4D2D2) across all screens
- **FR-002**: The app MUST define a complete light colour scheme using the green palette for primary, secondary, and tertiary colour roles
- **FR-003**: The app MUST NOT use dynamic colour (Material You) theming so that the brand colours are always used regardless of the device wallpaper
- **FR-004**: The app MUST display the BioGraph leaf logo at the top-center of the Onboarding screen, above the headline and setup controls
- **FR-005**: The app MUST display a small leaf logo icon alongside "BioGraph" text (icon-wordmark combo) in the Calendar screen's top app bar title area
- **FR-006**: The app MUST update the launcher icon to use the brand green palette and grey background
- **FR-007**: All text displayed over coloured surfaces MUST meet WCAG AA contrast minimums (4.5:1 for normal text, 3:1 for large text and UI components)
- **FR-008**: The green palette MUST be applied consistently to all interactive elements: buttons, FABs, toggles, chips, selected states, and progress indicators
- **FR-009**: The brand grey (#D4D2D2) MUST be used as a subtle background accent colour (e.g., surface variant or card backgrounds) rather than the primary background, to maintain a clean modern feel

## Assumptions

- The existing green colours in the resources (#15603E darkest, #1E8D5A mid, #6CC19F lightest) and grey (#D4D2D2) are the finalised brand palette — no additional colours need to be introduced
- The existing `black_logo.xml` vector drawable (leaf motif using green_mid and green_lightest) is the finalised logo asset to use throughout the app
- Dark mode is explicitly out of scope for this feature and will be addressed in a future iteration
- The app currently uses Material 3 with Jetpack Compose — theming changes will be applied through the Compose theme system and Material colour scheme roles
- No new typography or font changes are needed as part of this feature — the focus is on colour and logo placement only

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of screens (Onboarding, Calendar, Data Type List, Settings) display the green brand palette with no remaining purple/pink default colours visible
- **SC-002**: The BioGraph logo is visible on the Onboarding screen and Calendar top app bar, rendering correctly across phone-sized screens (360dp–430dp width)
- **SC-003**: All text-on-colour combinations in the app meet WCAG AA contrast ratio of at least 4.5:1 for body text and 3:1 for large text/UI components
- **SC-004**: The launcher icon reflects the brand green palette and renders correctly in round, squircle, and square adaptive icon shapes
- **SC-005**: The app displays its own brand colours on all Android versions (including Android 12+ devices where dynamic colour would otherwise override the palette)
- **SC-006**: The visual theme is perceived as cohesive — a walkthrough of all screens shows a consistent colour story with no jarring colour mismatches
