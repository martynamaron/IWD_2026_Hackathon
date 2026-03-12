package com.martynamaron.biograph.data

data class OnboardingSuggestion(
    val emoji: String,
    val description: String,
    val selectedByDefault: Boolean = false
)

val DEFAULT_SUGGESTIONS = listOf(
    OnboardingSuggestion("🩸", "Period / bleeding", selectedByDefault = true),
    OnboardingSuggestion("😊", "Mood"),
    OnboardingSuggestion("💊", "Medication taken"),
    OnboardingSuggestion("😴", "Sleep quality"),
    OnboardingSuggestion("🏃", "Exercise"),
    OnboardingSuggestion("🚪", "Left the house"),
    OnboardingSuggestion("👥", "Saw friends"),
    OnboardingSuggestion("🤕", "Headache"),
    OnboardingSuggestion("⚡", "Energy level")
)
