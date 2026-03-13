# Data Model: Brand Theme Refresh

**Feature**: 005-brand-theme-refresh  
**Date**: 2026-03-13

## Overview

This feature involves **no data model changes**. All modifications are to the UI theme layer (colour definitions, composable layouts, and resource files). No entities, database tables, Room DAOs, or repositories are added or modified.

## Entities

*None* — this is a purely visual/UI feature.

## State Changes

The only "state" affected is the Material 3 `ColorScheme` object, which is a compile-time constant (not persisted data):

| Component | Before | After |
|-----------|--------|-------|
| `Color.kt` | Purple80, PurpleGrey80, Pink80, Purple40, PurpleGrey40, Pink40 | GreenDarkest, GreenMid, GreenLightest, GrayBackground + derived tones |
| `Theme.kt` `LightColorScheme` | Purple/pink primary/secondary/tertiary | Green palette primary/secondary/tertiary + grey surfaceVariant |
| `Theme.kt` dynamic colour | Enabled (`true`) | Disabled (always use brand palette) |
| `Theme.kt` dark theme | Present but minimal | Removed (out of scope) |
