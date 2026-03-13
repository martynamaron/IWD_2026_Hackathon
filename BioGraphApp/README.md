![BioGraph Splash](brand/bioGraph_splash.gif)

# BioGraph

**BioGraph** is a personal daily tracking app for Android that helps users log and discover patterns in their daily habits, health, and wellness — all on-device, with full privacy.

Built with **Jetpack Compose** and **Kotlin**, BioGraph lets you define custom data types, track them on a calendar, and surfaces AI-powered insights about correlations in your data over time.

## Features

### Custom Data Types
Define anything you want to track using an emoji and a short description. Three input types are supported:
- **Toggle** — simple yes/no (e.g., 💊 Medication taken)
- **Scale** — 0–5 discrete steps (e.g., 😊 Mood)
- **Multiple Choice** — multi-select emoji chips (e.g., 🏃 Running, 🏊 Swimming, 🎾 Tennis, 💃 Dancing)

Data types are fully editable and deletable. Toggle types can be upgraded to Scale or Multiple Choice.

### Calendar Tracking
A month-view calendar lets you tap any day to open a panel where you toggle, rate, or select entries for each of your data types. Days with logged data show visual indicators on the calendar grid.

### AI-Powered Insights
An insights panel on the Calendar screen analyses your tracked data and surfaces plain-language correlation observations — e.g., *"You had headaches on 80% of the days you didn't leave the house."* Insights include:
- **Correlation strength** labels (Strong / Moderate / Mild)
- **Trend indicators** (Strengthening ↑ / Weakening ↓ / Stable →) showing how patterns evolve over time
- **Time period filtering** (Last Month, Last 3 Months, Last Year)
- **Sorting** by correlation strength or grouped by data type

All insights are strictly observational — no medical advice is ever provided.

### Onboarding
First-time users are guided through selecting from pre-defined tracking suggestions (Health, Medication, Mood, Energy Levels, Exercise, Period Bleeding) to get started quickly.

### Mock Data Generation
A built-in demo tool generates 3 or 6 months of realistic sample data with embedded health correlations (e.g., missed medication → dizziness, exercise → fatigue adaptation over time, menstrual cycle patterns). This showcases the app's insight capabilities without requiring weeks of real tracking.

### Brand Theme
A custom green colour palette with the BioGraph leaf logo, using the **Syne** typeface for a modern, nature-inspired look.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModels + Repositories) |
| Local Storage | Room (SQLite) |
| Navigation | Navigation Compose |
| Annotation Processing | KSP |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

## License

This project was built at the **IWD 2026 Hackathon**.
The [Syne](https://fonts.google.com/specimen/Syne) typeface is licensed under the [SIL Open Font License](Syne/OFL.txt).
