# Quickstart: Mock Data Generation & Insight Trends

**Feature**: 007-mock-data-trends  
**Date**: 13 March 2026

## Prerequisites

- Android Studio with Kotlin 2.0.21+
- AGP 9.0.1
- Device/emulator running API 24+

## Build & Run

```bash
cd BioGraphApp
./gradlew assembleDebug
# Install on connected device/emulator
./gradlew installDebug
```

## Verify Mock Data Generation

1. Launch the app.
2. When prompted (or via settings), choose "Generate sample data".
3. Select **3 months** or **6 months**.
4. Observe the calendar populates with entries for Health, Medication, Mood, Energy Levels, Exercise, and Period Bleeding.
5. Scroll through days — entries should vary naturally, not be identical each day.

## Verify Correlations

1. After generating 6 months of data, navigate to the calendar view.
2. Scroll down to the **Insights** panel.
3. Select **Last 3 months** or **Last year** tab.
4. Expected insights (may vary in wording):
   - Medication missed days correlate with feeling dizzy
   - Consistent medication use correlates with better mood
   - Period bleeding entries appear every ~28 days
   - Low mood/energy around period start
   - Exercise correlates with tiredness (early data)

## Verify Trend Indicators

1. With 6 months of data, select **Last 3 months** or **Last year**.
2. Look for trend indicators on insight cards:
   - Exercise–tiredness correlation should show **↓ Weakening**.
   - Medication–mood correlation may show **↑ Strengthening**.
3. Switch to **Last month** — no trend indicators should appear.

## Key Files

| Purpose | Path |
|---------|------|
| Mock data generator | `app/src/main/java/.../util/MockDataGenerator.kt` |
| Insight ViewModel | `app/src/main/java/.../viewmodel/InsightViewModel.kt` |
| Insight card UI | `app/src/main/java/.../ui/components/InsightCard.kt` |
| Correlation engine | `app/src/main/java/.../data/analysis/CorrelationEngine.kt` |
