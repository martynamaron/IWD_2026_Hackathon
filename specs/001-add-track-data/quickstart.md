# Quickstart: Add and Track Data

**Date**: 2026-03-12 | **Feature Branch**: `001-add-track-data`

## Prerequisites

- Android Studio Ladybug (2024.2+) or newer
- JDK 11+
- Android SDK with API 36 installed
- An Android emulator or device running API 24+

## Build & Run

```bash
cd BioGraphApp
./gradlew assembleDebug
```

Install on connected device/emulator:
```bash
./gradlew installDebug
```

Or open `BioGraphApp/` in Android Studio and run the `app` configuration.

## Project Layout

```
BioGraphApp/app/src/main/java/com/martynamaron/biograph/
├── MainActivity.kt                     # Single activity, hosts Compose NavHost
├── BioGraphApplication.kt             # Application class, Room DB singleton
├── data/local/                         # Room entities + DAOs
├── data/repository/                    # Repository layer
├── viewmodel/                          # ViewModels (MVVM)
├── ui/navigation/                      # NavGraph with AnimatedNavHost
├── ui/screens/calendar/                # Calendar + day panel
├── ui/screens/datatype/                # Data type management
├── ui/screens/onboarding/              # First-run flow
├── ui/screens/settings/                # Mock data generation
├── ui/components/                      # Reusable composables
├── ui/theme/                           # Material 3 theme (existing)
└── util/                               # Mock data generator
```

## Key Dependencies Added

| Dependency | Purpose |
|-----------|---------|
| `androidx.room:room-runtime` | Local SQLite database |
| `androidx.room:room-compiler` (KSP) | Room annotation processing |
| `androidx.room:room-ktx` | Kotlin coroutine/Flow extensions |
| `androidx.navigation:navigation-compose` | Screen navigation + transitions |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | ViewModel integration |
| `androidx.lifecycle:lifecycle-runtime-compose` | `collectAsStateWithLifecycle()` |
| `com.android.tools:desugar_jdk_libs` | java.time on API < 26 |

## Architecture Pattern

**MVVM with unidirectional data flow**:

```
[Room DB] → [Repository] → [ViewModel (StateFlow)] → [Composable (collectAsStateWithLifecycle)]
                                    ↑
                          [User actions / events]
```

- **Composables** are stateless — they render state and emit events.
- **ViewModels** hold `StateFlow<UiState>` and process user actions.
- **Repositories** abstract Room DAOs and provide `Flow<T>` streams.
- **Room** handles persistence, query invalidation, and cascade deletes.

## Database Schema

Two tables:
- `data_types` — user-defined tracking categories (emoji + description)
- `daily_entries` — date × data_type toggle records (presence = active)

See [data-model.md](data-model.md) for full schema details.

## App Flow

1. **First launch** → Onboarding screen with pre-defined suggestions
2. **Main screen** → Monthly calendar with day indicators
3. **Tap a day** → Bottom sheet with data type toggles + Save button
4. **Manage data types** → List screen with add/edit/delete actions
5. **Settings** → Mock data generation (2 months of random entries)
