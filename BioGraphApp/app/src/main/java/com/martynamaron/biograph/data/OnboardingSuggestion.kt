package com.martynamaron.biograph.data

data class OptionSuggestion(val emoji: String, val label: String)

data class OnboardingSuggestion(
    val emoji: String,
    val description: String,
    val selectedByDefault: Boolean = false,
    val inputType: InputType = InputType.TOGGLE,
    val defaultOptions: List<OptionSuggestion> = emptyList()
)

val DEFAULT_SUGGESTIONS = listOf(
    OnboardingSuggestion("🩸", "Period / bleeding", selectedByDefault = true),
    OnboardingSuggestion("😊", "Mood", inputType = InputType.SCALE),
    OnboardingSuggestion("💊", "Medication taken"),
    OnboardingSuggestion("😴", "Sleep quality"),
    OnboardingSuggestion(
        "🏃", "Exercise",
        inputType = InputType.MULTIPLE_CHOICE,
        defaultOptions = listOf(
            OptionSuggestion("🏃", "Running"),
            OptionSuggestion("🏊", "Swimming"),
            OptionSuggestion("🎾", "Tennis"),
            OptionSuggestion("💃", "Dancing")
        )
    ),
    OnboardingSuggestion("🚪", "Left the house"),
    OnboardingSuggestion("👥", "Saw friends"),
    OnboardingSuggestion("🤕", "Headache"),
    OnboardingSuggestion("⚡", "Energy level")
)
